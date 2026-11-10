#version 330

precision highp float;

// SvregBlue: copies the scene colour so the grade pass can read it

layout(std140) uniform DynamicTransforms {
    highp mat4 ModelViewMat;
    highp vec4 ColorModulator;
    highp vec3 ModelOffset;
    highp mat4 TextureMat;
};

uniform highp sampler2D Sampler0;

in highp vec2 uv;

out highp vec4 fragColor;

void main() {
    fragColor = vec4(texelFetch(Sampler0, ivec2(gl_FragCoord.xy), 0).rgb, 1.0);
}
