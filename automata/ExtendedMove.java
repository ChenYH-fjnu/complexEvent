/**
 * SVPAlib
 * automata
 * Apr 21, 2015
 * @author Loris D'Antoni
 */
package automata;

import java.util.List;

import org.sat4j.specs.TimeoutException;

import theory.BooleanAlgebra;

/**
 * Abstract automaton move
 * @param <P> set of predicates over the domain S
 * @param <S> domain of the automaton alphabet
 */
public abstract class ExtendedMove<P, S> {

	// Source state
	public Integer from;
	// Target state
	public Integer to;
	
	public Integer lookahead;
	
	/**
	 * Transition from state <code>from</code> to state <code>to</code>
	 */
	public ExtendedMove(Integer from, Integer to) {
		this.from = from;
		this.to = to;
	}

	/**
	 * @return whether the transition can ever be enabled
	 */
	public abstract boolean isSatisfiable(BooleanAlgebra<P, S> ba);

	/**
	 * @return an input triggering the transition. Null if it's an epsilon
	 *         transition
	 * @throws TimeoutException 
	 */
	public abstract List<S> getWitness(BooleanAlgebra<P, S> ba) throws TimeoutException;

	/**
	 * @return true iff <code>input</code> can trigger the transition
	 * @throws TimeoutException 
	 */
	public abstract boolean hasModel(List<S> input, BooleanAlgebra<P, S> ba) throws TimeoutException;

	/**
	 * @return true iff it is an epsilon transition
	 */
	public abstract boolean isEpsilonTransition();

	/**
	 * Create the dot representation of the move
	 */
	public abstract String toDotString();

}
