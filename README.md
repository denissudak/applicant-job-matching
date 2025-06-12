Supporting code for my article - Applying matching algorithms to real allocation
problems, [Part 1](https://denissudak.substack.com/p/applying-matching-algorithms-to-real)
and [Part 2](https://denissudak.substack.com/p/applying-matching-algorithms-to-real-8e7)

Go to `ApplicantJobMatching` to run examples from the article.

`TeamNetwork` builds the flow network and finds the maximum flow, which it then converts into a matching. I use
the [push-relabel maximum flow algorithm](https://github.com/denissudak/max-flow).
It’s open source, in case you want to explore it further.

I hope you find it useful. Enjoy!

