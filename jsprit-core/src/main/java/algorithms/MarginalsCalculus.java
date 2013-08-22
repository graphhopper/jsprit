package algorithms;

import basics.route.TourActivity;

interface MarginalsCalculus {
	
	Marginals calculate(InsertionFacts iFacts, TourActivity prevAct, TourActivity nextAct, TourActivity newAct);

}
