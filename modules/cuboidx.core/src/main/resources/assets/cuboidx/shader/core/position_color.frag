#version 330

in vec4 vertexColor;

out vec4 FragColor;

uniform vec4 ColorModulator;

void main() {
    vec4 color = vertexColor * ColorModulator;
    if (color.a < 0.1) discard;
    FragColor = color;
}
