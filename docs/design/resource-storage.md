# Resource Storage System

**Status:** Draft · **Module:** `room-upgrades` (`storage` source set) · **Last updated:** 2026-08-04

> Design notes for the resource-storage layer that lets Compact Machines rooms
> discover, name, and refer to block resource handlers (item/fluid inventories,
> etc.) so those handlers can be targeted from room upgrades and surfaced in the
> room-management UIs.

## Motivation

Room upgrades and the room-management UIs need a stable way to talk about "the
chest in the corner" or "the tank by the door" without holding a live reference
to the block. We want to:

- **Query** the resource handlers available inside a room.
- **Name** them (user-assignable, human-friendly labels).
- **Refer** to them by name from upgrade configuration and UI, persisting those
  references across save/load and across the block being unloaded.

The key constraint: a resource handler is a *live, non-serializable* object. What
we persist and pass around must be a **coordinate + how to resolve it**, not the
handler itself.

## Core concept: `NamedResourceStorage`

`NamedResourceStorage` is a named, positioned *handle* to a block's resource
handler. It stores:

| Field     | Type                        | Purpose |
|-----------|-----------------------------|---------|
| `name`    | `String`                    | User-assignable, human-friendly label. |
| `storage` | `ResourceStoragePosition<T>`| Where the handler lives + how to resolve it. |

It is **not** a container for the handler. The handler is resolved on demand via
`resolve(MinecraftServer)`, because handlers are live objects owned by the block
and can change or disappear.

The record is generic over the resource type: `NamedResourceStorage<T extends
Resource>`, where `T` is e.g. `ItemResource` or `FluidResource`. `resolve`
returns a `ResourceHandler<T>`.

### `ResourceStoragePosition` — the shared location core

`NamedResourceStorage` (a named handle) and `LocatedResourceStorage` (a resolved,
live pairing produced by a scan) share their entire "where + which kind" half.
That half is factored into `ResourceStoragePosition<T>`:

| Field       | Type                    | Purpose |
|-------------|-------------------------|---------|
| `position`  | `GlobalPos`             | Dimension + block position of the backing block. |
| `direction` | `@Nullable Direction`   | Side to query the capability from (`null` = unsided). |
| `type`      | `ResourceStorageType<T>`| The registered storage kind (carries the capability). |

It owns `resolve(MinecraftServer)` and the field codecs. Both records compose one
(`NamedResourceStorage(name, storage)`, `LocatedResourceStorage(storage, handler)`)
and expose thin `position()` / `direction()` / `type()` delegators. Its codec is
published as a `MapCodec` so `NamedResourceStorage` inlines the location keys
*flatly* — the on-disk shape stays `{ name, position, direction?, type }`, not a
nested object.

## The NeoForge resource + capability API

This module targets the modern NeoForge transfer API, not the legacy
`IItemHandler`:

- **`net.neoforged.neoforge.transfer.resource.Resource`** — marker base type for
  a *kind* of resource. Concrete types: `ItemResource`
  (`net.neoforged.neoforge.transfer.item`), `FluidResource`
  (`net.neoforged.neoforge.transfer.fluid`).
- **`net.neoforged.neoforge.transfer.ResourceHandler<T extends Resource>`** — the
  live handler (an inventory/tank view). This is the "item handler" analogue.
- **`net.neoforged.neoforge.capabilities.BlockCapability<T, C>`** — a flexible,
  registry-backed accessor for objects of type `T` at a block, with additional
  context `C`. Queried via `Level#getCapability(BlockCapability, BlockPos, C)`,
  which returns `@Nullable T`.
- **`net.neoforged.neoforge.capabilities.Capabilities`** — NeoForge's built-in
  capability constants.

The built-in block capabilities relevant to us:

```java
Capabilities.Item.BLOCK   // BlockCapability<ResourceHandler<ItemResource>,  @Nullable Direction>
Capabilities.Fluid.BLOCK  // BlockCapability<ResourceHandler<FluidResource>, @Nullable Direction>
Capabilities.Energy.BLOCK // BlockCapability<EnergyHandler, @Nullable Direction>  — NOT a ResourceHandler
```

### Key facts that shape the design

1. **Capabilities are static, per-resource-type constants.** There is one
   `Capabilities.Item.BLOCK`, one `Capabilities.Fluid.BLOCK`. They are singletons
   interned in a registry keyed by `(Identifier name, Class type, Class context)`.
2. **A `BlockCapability` carries the concrete resource type in its own generics**
   — `Capabilities.Item.BLOCK` is typed to `ResourceHandler<ItemResource>`, full
   stop. There is no "generic" capability that yields `ResourceHandler<T>` for an
   arbitrary `T`.
3. **Generics are erased at runtime.** A `NamedResourceStorage<T>` carries *zero*
   runtime information about `T`. Given only `T`, there is no way to pick the
   right capability — the type variable does not exist at the point `resolve`
   runs.
4. **Energy is not a `ResourceHandler`.** `Capabilities.Energy.BLOCK` yields an
   `EnergyHandler`, which does not implement `ResourceHandler` and whose resource
   type is not a `Resource`. The `T extends Resource` bound deliberately excludes
   energy; if energy storage is wanted later it needs a parallel, non-generic
   path (see Open questions).

### Consequence for `resolve`

Because of facts (2) and (3), **`resolve` cannot derive the capability from `T`**.
The record must carry a *runtime witness* that ties `T` to a concrete
capability. Two viable shapes:

- **A. Store the `BlockCapability` directly** on the record:
  `BlockCapability<ResourceHandler<T>, @Nullable Direction> capability`. Minimal
  and type-safe; `resolve` just calls
  `level.getCapability(capability, position.pos(), direction)`.
- **B. Store a small `ResourceStorageType<T>` descriptor** that wraps the
  capability *plus* an `Identifier` id, a `Codec<T>`/`StreamCodec` for the
  resource, and a display name. This is the codec- and UI-friendly form: we
  serialize the type's id, not the capability object, and reconstruct by id.

Option A is the minimal correct fix for the current compile error. **Option B is
what we implemented**, since it also solves "how do I serialize which kind of
storage this is."

### `ResourceStorageType` (implemented)

`ResourceStorageType<T extends Resource>` is a registry object
(`ResourceStorageType.REGISTRY_KEY` → `compactmachines:resource_storage_type`,
synced) that bundles:

- `BlockCapability<ResourceHandler<T>, @Nullable Direction> blockCapability` — the
  witness used to resolve the handler.
- `Codec<T> resourceCodec` / `StreamCodec<RegistryFriendlyByteBuf, T>
  resourceStreamCodec` — for (de)serializing resources of this kind, needed once
  upgrades/UI persist references to specific resources.

The witness lives on `ResourceStoragePosition<T>` (which `NamedResourceStorage`
and `LocatedResourceStorage` compose) as a `ResourceStorageType<T>` rather than a
raw capability. Because the type is registered, persisting a handle serializes the
type's id (via the registry) rather than the capability object.

`ResourceStorageTypes` (in the `storage` source set) declares the
`DeferredRegister` and the two built-in kinds, wired from `RoomUpgrades.init`:

| Id                          | Resource       | Capability                 |
|-----------------------------|----------------|----------------------------|
| `compactmachines:item`      | `ItemResource` | `Capabilities.Item.BLOCK`  |
| `compactmachines:fluid`     | `FluidResource`| `Capabilities.Fluid.BLOCK` |

## Open questions / future work

- **Discovery / query.** `ResourceStorageScanner<T>` is the low-level primitive,
  a fluent builder: `forResource(type).inBoundaries(aabb).filter(positions)
  .sides(...).scan(level)` yields `LocatedResourceStorage<T>` — a resolved, live
  (position + handler) pairing, with `.named(String)` to turn one into a
  persistable `NamedResourceStorage`. The steps are separate concerns (resource /
  region / position filter / sides) so the API can grow without new overloads.
  (Generalized out of the tree-cutter upgrade's old item-only corner scan.)

- **Per-room caching.** `RoomStorageCache` sits above the scanner: it caches the
  discovered *positions* per `ResourceStorageType` and only rescans the room's
  inner bounds once per refresh interval (`DEFAULT_REFRESH_TICKS` = 100 = 5 s).
  Live handlers are re-resolved on every `resources(room, type)` call, so the
  costly volume scan is throttled while returned handlers never go stale (a block
  removed between scans resolves to `null` and is dropped; one added is picked up
  next scan). It is reached via the room capability
  `StorageCapabilities.ROOM_STORAGE` (`room.getCapability(...)`), whose provider
  keeps one cache per `(server, room)` (weak-keyed by server). The tree-cutter
  upgrade now queries this instead of scanning every tick.

- **Still open:** the scan region is currently the whole inner bounds with only
  the unsided capability probed; a smarter scan strategy (block-entity iteration
  vs. tracked set, configurable sides) and an invalidation story tied to
  `Level#invalidateCapabilities` (rather than only the time interval) would cut it
  further. Cache eviction is currently just the weak server key — a
  server-stopping hook would be tidier.
- **Naming + persistence.** Where names live (per-room attachment?) and
  uniqueness rules. The wire/disk format is settled: `NamedResourceStorage.CODEC`
  / `STREAM_CODEC` serialize `{ name, GlobalPos, Direction?, ResourceStorageType
  id }` — the type id resolves back through the registry to the capability and
  resource codecs. Decoded handles are wildcard-typed (`NamedResourceStorage<?>`)
  since the resource type is only known dynamically from the registry;
  `ResourceStorageType` also exposes its own `CODEC` (registry `byNameCodec`, lazy)
  and `STREAM_CODEC` (`ByteBufCodecs.registry`).
- **Referencing from upgrades/UI.** A by-name reference type that upgrades store
  and resolve lazily; UI for listing/renaming storages.
- **Energy and other non-`Resource` handlers.** Decide whether to generalize
  beyond `ResourceHandler<T>` (energy, custom handlers) or keep those on a
  separate track.
- **Caching.** For repeated resolution, prefer `BlockCapabilityCache` over
  repeated `Level#getCapability` calls.
