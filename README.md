Supporting code for my article - [Applying matching algorithms to real allocation problems](https://medium.com/@denissudak/applying-matching-algorithms-to-real-allocation-problems-419bd12f7449)

To run the example from the article, go to `ApplicantJobMatching`.

`TeamNetwork` builds the flow network and finds the maximum flow, which it then converts into a matching. I use the [push-relabel maximum flow algorithm](https://github.com/denissudak/max-flow). 
It’s open source, in case you want to explore it further.

I hope you find it useful. Enjoy!

