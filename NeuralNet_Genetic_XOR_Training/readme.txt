This is an experimental Neural Net implementation used to train an XOR gate.
While the net is flexible, and allows for any number of inputs, outputs, and hidden layers of any size,
the genetic algorithm used for training seems to fail with a higher amount of weights to adjust.
Training is done by assessing an error (sum of squares of differences between expected and output value)
of sample in a set, and then crossbreeding those that have the smallest error values.
(by using pie chart selection those individuals with highest fitness have also a highest chance of being picked)

Results:
6,52644283084536E-05 ~= 0 for inputs 0 0
0
0,999945214116845 ~= 1 for inputs 0 1
1
0,999951466649841 ~= 1 for inputs 1 0
1
0,000105403647881919 ~0 for inputs 1 1
0
It may be necessary to restart the application if the results at the end do not match the expected values (fitness stagnates at around 400).
It seems that this implementation of genetic neural net training has some essential flaws.
Potential fixes:
Implementing the island algorithm ( n islands with separate breeding and a best specimen sent every k generations.
Implementing energy - a specimen dies after depleting it's energy, energy falls proportionally to error.