package iridia;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

/**
 * HyperHeuristic class just to override hasTimeExpired and make it public.
 */
public class MyHyperHeuristic extends HyperHeuristic {

    public MyHyperHeuristic(long seed) {
        super(seed);
    }

    @Override
    public boolean hasTimeExpired() {
        return super.hasTimeExpired();
    }

    @Override
    protected void solve(ProblemDomain pd) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
