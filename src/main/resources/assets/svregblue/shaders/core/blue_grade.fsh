#version 330

precision highp float;

// SvregBlue "Blue Grading": the blue-hour look of the references.
//  - brightness is mapped onto a blue ramp: navy shadows -> ultramarine mid tones -> icy pale highlights
//  - black is lifted to navy (Bayangan Navy), so nights read as deep blue, not as a black screen
//  - a little of the original colour stays, so grass/water/stone are still told apart
//  - warm lights (torches, lanterns, fire) keep their orange, as small warm spots in the blue world
// TextureMat[0] = blue level (0..1), navy lift (0..1), keep warm lights (0/1), 0

layout(std140) uniform DynamicTransforms {
    highp mat4 ModelViewMat;
    highp vec4 ColorModulator;
    highp vec3 ModelOffset;
    highp mat4 TextureMat;
};

uniform highp sampler2D Sampler0; // scene colour copy

in highp vec2 uv;

out highp vec4 fragColor;

const highp vec3 LW = vec3(0.30, 0.59, 0.11);

// the blue ramp, picked from the three reference photos
const highp vec3 NAVY  = vec3(0.020, 0.035, 0.120); // under the tree, deep sea
const highp vec3 ULTRA = vec3(0.090, 0.210, 0.620); // the sea and the sky band
const highp vec3 CYAN  = vec3(0.300, 0.620, 0.860); // the foggy road
const highp vec3 ICE   = vec3(0.820, 0.910, 1.000); // foam, white flowers

highp vec3 ramp(highp float l) {
    if (l < 0.30) return mix(NAVY, ULTRA, l / 0.30);
    if (l < 0.65) return mix(ULTRA, CYAN, (l - 0.30) / 0.35);
    return mix(CYAN, ICE, (l - 0.65) / 0.35);
}

void main() {
    highp vec3 col = texelFetch(Sampler0, ivec2(gl_FragCoord.xy), 0).rgb;
    highp float level = clamp(TextureMat[0].x, 0.0, 1.0);
    highp float lift = clamp(TextureMat[0].y, 0.0, 1.0);

    highp float lum = clamp(dot(col, LW), 0.0, 1.0);
    // lift the dark end (navy shadows): raise the curve's foot, keep the top
    highp float l = mix(lum, pow(lum, 0.72), lift);
    l = l + (1.0 - l) * 0.10 * lift * (1.0 - lum);
    highp vec3 blue = ramp(clamp(l, 0.0, 1.0));

    // keep a bit of the original hue as a soft tint over the ramp, so materials still read
    highp vec3 hue = col / max(lum, 1e-3);
    blue *= mix(vec3(1.0), clamp(hue, 0.6, 1.4), 0.18);

    // warm lights: bright, clearly orange pixels keep most of their colour
    highp float warm = clamp((col.r - col.b) * 3.0, 0.0, 1.0) * smoothstep(0.20, 0.55, lum) * TextureMat[0].z;

    highp float k = level * (1.0 - 0.85 * warm);
    fragColor = vec4(mix(col, blue, k), 1.0);
}
