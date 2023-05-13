#version 330

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 texCoord0;

uniform mat4 ProjectionMatrix, ModelViewMatrix;

void main() {
    gl_Position = ProjectionMatrix * ModelViewMatrix * vec4(Position, 1.0);
    vertexColor = Color;
    texCoord0 = UV0;
}
