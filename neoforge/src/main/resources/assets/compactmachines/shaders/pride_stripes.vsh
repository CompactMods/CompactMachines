#version 450

#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
#include <minecraft:globals.glsl>

// POSITION_COLOR_NORMAL vertex format:
//   Position — vertex position in camera-relative world space (the BER's pose
//              stack is translated to the block, so this lands at world coords
//              minus the camera position).
//   Color    — currently unused by the pride shader (BER writes 255 alpha and
//              block-local position in RGB so downstream shaders that *do*
//              want the in-block point can still read it).
//   Normal   — outward face normal of the pane the vertex belongs to.
layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec3 Normal;

// Absolute world position — recovered by re-adding the camera position from
// the Globals UBO. Sampling noise here means a wall of machines reads as a
// single continuous surface rather than each block restarting the pattern.
//
// Sign matters: per terrain.vsh's
//   `pos = Position + (ChunkPosition - CameraBlockPos) + CameraOffset`
// CameraOffset is stored as the *negated* fractional camera position
// (so terrain math comes out camera-relative). The actual camera world position
// is therefore `CameraBlockPos - CameraOffset`, and `Position` is already
// camera-relative (BER dispatcher translates the pose to block-minus-camera).
// So:  worldPos = Position + CameraBlockPos - CameraOffset.
layout(location = 0) out vec3 worldPos;
layout(location = 1) out vec3 faceNormal;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    worldPos = Position + vec3(CameraBlockPos) - CameraOffset;
    faceNormal = Normal;
}
