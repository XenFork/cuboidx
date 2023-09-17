#version 330

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 FragColor;

uniform vec4 ColorModulator;
uniform sampler2D Sampler0;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) discard;
    FragColor = color;
}
