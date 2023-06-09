#version 330

in vec3 Position;
in vec4 Color;

out vec4 vertexColor;

uniform mat4 ProjectionMatrix, ModelViewMatrix;

void main() {
    gl_Position = ProjectionMatrix * ModelViewMatrix * vec4(Position, 1.0);
    vertexColor = Color;
}
