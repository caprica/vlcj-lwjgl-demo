vlcj-lwjgl-demo
===============

This project shows one way to embed a media player inside an OpenGL application. In this demo application the
[LWJGL](https://www.lwjgl.org/) library is used, but the same approach should work with any general Java OpenGL library.

Important
---------
 
Support for rendering via OpenGL requires VLC 4.0.0 - this version of VLC is not yet generally available, if you want to
use it you must build the latest VLC from its sources, or use a prebuilt development snapshot if you can find one.

You also need to use the vlcj-5.x development branch, OpenGL rendering is not supported in vlcj-4.x (vlcj-4.x uses VLC
3.0.0).

Resizing Video
--------------

Resizing video is a *client* problem. The OpenGL callbacks render at the video's intrinsic dimensions, if you need
resize behaviour then that is a problem that you must solve in your own application.

This example implements resizing (optionally via a flag in the code) by resetting the viewport to match the video
size in a framebuffer size callback.

