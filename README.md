Supporting code for my article - Applying matching algorithms to real allocation
problems, [Part 1](https://medium.com/@denissudak/applying-matching-algorithms-to-real-allocation-problems-419bd12f7449)
and [Part 2](https://medium.com/@denissudak/applying-matching-algorithms-to-real-allocation-problems-part-2-8088ef69e827)

Go to `ApplicantJobMatching` to run examples from the article.

`TeamNetwork` builds the flow network and finds the maximum flow, which it then converts into a matching. I use
the [push-relabel maximum flow algorithm](https://github.com/denissudak/max-flow).
It’s open source, in case you want to explore it further.

I hope you find it useful. Enjoy!

