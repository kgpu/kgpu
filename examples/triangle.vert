#version 450

out gl_PerVertex {
    vec4 gl_Position;
};

layout(location=0) in vec3 positions;
layout(location=1) in vec3 v_colors;

layout(location=0) out vec3 f_colors;


void main() {
    f_colors = v_colors;
    gl_Position = vec4(positions, 1.0);
}
