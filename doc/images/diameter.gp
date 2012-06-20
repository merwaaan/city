set terminal png
set output 'diameter.png'

set nokey
set xlabel 'Temps'
set ylabel 'Diamètre'

plot "diameter.dat" with lines
