# SkipList
Multicore project Concurrent SkipList

# Directories
* `src`, Implementations of concurrent skip lists, test codes and benchmark codes.

          etc. *`LockFree`, Implementations of lock free skip list
               *`Test`, test case codes and benchmark codes.
* `paper`, Term Paper and corresponding graphs we plot.

* Presentation.pptx

# Compile

* Simply clone git or unzip the zipped file. Go into `src`->`Test`->`SKipListTestBench.java`.
Run for permance test. Runtime results will be printed out in console.
Modify `setupContexts` for different thread numbers and ranges. Can also select to average the runtime for different times.
Modify `runAllTests` for different number of operations and different percentages of operations types.

