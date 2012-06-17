set terminal png
set output 'area.png'

set nokey
set xlabel 'Temps'
set ylabel 'Superficie'

plot "area.dat" with lines
