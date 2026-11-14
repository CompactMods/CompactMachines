#version 450

#extension GL_ARB_separate_shader_objects : require

// tye_dye — domain-warped noise palette gradient. Reads as a slow lava-lamp
// turbulence with a ROY G BIV default palette.

#include <minecraft:globals.glsl>

layout(location = 0) in vec3 worldPos;   // absolute world coordinates; identical at the seam
                                         // between adjacent machines, so the noise field is
                                         // continuous across the wall.
layout(location = 1) in vec3 faceNormal; // outward normal of the pane (kept so future shaders can
                                         // branch on face direction without a vertex format change).

layout(location = 0) out vec4 fragColor;

const float OPACITY = 0.4; // matches the tinted-glass look the user dialled in.

// Palette — default is a saturated ROY G BIV spectrum so the base case reads
// as "rainbow lava lamp". Customise by editing these constants and bumping
// PALETTE_SIZE. The shader supports up to 10 colours without further changes;
// trailing zeros are ignored.
const int PALETTE_SIZE = 7;
const vec3 PALETTE[10] = vec3[10](
    vec3(1.000, 0.000, 0.000), // Red
    vec3(1.000, 0.498, 0.000), // Orange
    vec3(1.000, 1.000, 0.000), // Yellow
    vec3(0.000, 0.800, 0.000), // Green
    vec3(0.000, 0.000, 1.000), // Blue
    vec3(0.294, 0.000, 0.510), // Indigo
    vec3(0.580, 0.000, 0.827), // Violet
    vec3(0.0), vec3(0.0), vec3(0.0)
);

// --- noise -----------------------------------------------------------------

// 3D hash — Dave Hoskins' "hash33 → 1D" derivative; visibly less banded than
// the cheap multiplicative variant when the noise is sampled at low frequency.
float hash3(vec3 p) {
    p = fract(p * vec3(0.1031, 0.1030, 0.0973));
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

// Smoothed value noise. Uses quintic (Perlin's improved) interpolation so the
// transitions between lattice cells have continuous first AND second
// derivatives — kills the soft "seam" you get with cubic smoothstep.
float valueNoise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);

    float c000 = hash3(i + vec3(0.0, 0.0, 0.0));
    float c100 = hash3(i + vec3(1.0, 0.0, 0.0));
    float c010 = hash3(i + vec3(0.0, 1.0, 0.0));
    float c110 = hash3(i + vec3(1.0, 1.0, 0.0));
    float c001 = hash3(i + vec3(0.0, 0.0, 1.0));
    float c101 = hash3(i + vec3(1.0, 0.0, 1.0));
    float c011 = hash3(i + vec3(0.0, 1.0, 1.0));
    float c111 = hash3(i + vec3(1.0, 1.0, 1.0));

    return mix(
        mix(mix(c000, c100, f.x), mix(c010, c110, f.x), f.y),
        mix(mix(c001, c101, f.x), mix(c011, c111, f.x), f.y),
        f.z);
}

// 5-octave fBm. Extra octave + lower gain (0.55) lets fine wisps survive
// without turning the field into pure mush.
float fbm(vec3 p) {
    float v = 0.0;
    float a = 0.55;
    for (int i = 0; i < 5; i++) {
        v += a * valueNoise(p);
        p *= 2.07; // non-integer scaling avoids axis-aligned grid artefacts.
        a *= 0.5;
    }
    return v;
}

// Smoothly interpolate the palette. t ∈ [0,1] → blends the PALETTE_SIZE
// active entries with smoothstep on the segment-local parameter so palette
// boundaries don't read as creases.
vec3 samplePalette(float t) {
    float scaled = clamp(t, 0.0, 1.0) * float(PALETTE_SIZE - 1);
    int idx = int(floor(scaled));
    float f = scaled - float(idx);
    f = smoothstep(0.0, 1.0, f);
    int next = idx + 1;
    if (next > PALETTE_SIZE - 1) next = PALETTE_SIZE - 1;
    return mix(PALETTE[idx], PALETTE[next], f);
}

// --- main ------------------------------------------------------------------

void main() {
    // GameTime cycles 0..1 over 24000 ticks (20 min). Drift coefficients below
    // scale GameTime directly. Halved from the previous pass so a noise feature
    // takes roughly 6-12 seconds to cross — calmer "lava lamp" pace.
    float t = GameTime / 2;

    // Sample anchor is the absolute world position so adjacent machines blend
    // continuously. Frequency 0.6 means a noise feature spans ~1.5 blocks —
    // big patches on a multi-machine wall, ~half a patch on a single block.
    vec3 q = worldPos * 0.6;

    // Two drift vectors at different speeds/directions so the combined field
    // never just translates uniformly.
    vec3 driftA = vec3(t * 160.0, t * 235.0, t * 140.0);
    vec3 driftB = vec3(t * 270.0, t * 115.0, t * 190.0);

    // Domain warping: sample a coarse noise field to displace the main sample
    // position. Straight contours become curves, palette boundaries read as
    // wisps instead of edges.
    vec3 warpIn = q * 0.7 + driftB;
    vec3 warp = vec3(
        fbm(warpIn),
        fbm(warpIn.yzx + 9.31),
        fbm(warpIn.zxy + 4.17));

    float n = fbm(q + warp * 2.4 + driftA);

    // fBm here outputs roughly [0.25, 0.85]; remap with smoothstep so the
    // gradient stays soft at the extremes.
    n = smoothstep(0.25, 0.85, n);

    fragColor = vec4(samplePalette(n), OPACITY);
}
