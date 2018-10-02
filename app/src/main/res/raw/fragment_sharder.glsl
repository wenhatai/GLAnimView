#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES texture;
varying vec2 v_TexCoordinate;
varying vec2 v_TexAlphaCoordinate;

void main () {
    vec4 color = texture2D(texture, v_TexCoordinate);
    float r = texture2D(texture, v_TexAlphaCoordinate).r;
    float g = texture2D(texture, v_TexAlphaCoordinate).g;
    float b = texture2D(texture, v_TexAlphaCoordinate).b;
    float y = 0.257*r + 0.504*g + 0.098*b + 0.0625;
    gl_FragColor = color;
    gl_FragColor.a = y;
    gl_FragColor.r = gl_FragColor.r ;
    gl_FragColor.g = gl_FragColor.g ;
    gl_FragColor.b = gl_FragColor.b ;
}
