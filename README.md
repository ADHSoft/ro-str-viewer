ro-str-viewer
=============

Small library for reading and rendering effect files (`.str`) from Ragnarok Online MMORPG.
It contains also a small tool `com.skardach.ro.tools.STRViewer` that will allow you to read a given `.str` file and animate it.

This is a crude try by a guy who has little experience with Computer Graphics, let alone GLSL so be kind :wink:.

-------------

ADHsoft contribution:
Added .vce compatibility (djmax files, but must be converted to str first)
Fixed U,V coordinates management, GL blending, texture changes, rotation factor
Updated OpenGL libraries new names
Fixed crashes on special situations
Removed pinkRemover shader
