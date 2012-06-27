mencoder "mf://frames/lh/*.png" -mf fps=16:type=png -ovc lavc -lavcopts vcodec=mpeg4:vqscale=2:vhq:v4mv:trell:autoaspect -o video.avi -nosound -vf scale
