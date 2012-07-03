set terminal png
set output 'centrality.png'

set nokey
set xlabel 'Temps'
set ylabel 'Centralité moyenne'

plot "centrality.dat" with lines
