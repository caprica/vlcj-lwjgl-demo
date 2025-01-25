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

Demos
-----

There are two demo applications here.

 - VideoEngineDemo
 - VideoCubeDemo

The first `VideoEngineDemo` renders the video into an OpenGL window.

The second `VideoCubeDemo` is somewhat more low-level, and renders video into a dynamically updating texture on the face
of a cube in a 3D scene.
