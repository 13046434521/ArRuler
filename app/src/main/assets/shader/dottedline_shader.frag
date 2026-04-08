#version 300 es
precision mediump float;

layout(location=0) out vec4 o_FragColor;
uniform bool u_Point;// 非0即true
void main() {
    if(u_Point && length(gl_PointCoord - vec2(0.5))>0.5){
        discard;
    }

    o_FragColor = vec4(1.0f, 1.0f, 0.0f, 1.0f);
}
