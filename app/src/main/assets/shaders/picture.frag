/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#extension GL_OES_EGL_image_external : require

precision mediump float;
uniform sampler2D u_TextureUnit;
varying vec2 v_TexCoord;
uniform vec4 u_Color;
vec4 v_Color;

void main() {
    v_Color = texture2D(u_TextureUnit, v_TexCoord);
    if(v_Color.r ==1.0&&v_Color.g==1.0&&v_Color.b==1.0&&v_Color.a ==1.0){
         discard;
    }
    gl_FragColor = v_Color;
//    gl_FragColor = texture2D(u_TextureUnit, v_TexCoord);
}
