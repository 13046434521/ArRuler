#version 300 es

precision mediump float;
layout(location = 0) out vec4 o_FragColor;

void main() {
    if (length(gl_PointCoord - vec2(0.5,0.5)) > 0.5){
//        o_FragColor = vec4(0.0f, 1.0f, 0.0f, 1.0f);
        discard;
    }else{
        o_FragColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
