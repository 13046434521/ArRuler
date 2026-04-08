precision mediump float;
varying vec2 v_TexCoord;
//uniform sampler2D sTexture;
uniform samplerExternalOES u_CameraColorTexture;

void main() {
    gl_FragColor = texture2D(u_CameraColorTexture, v_TexCoord);
}