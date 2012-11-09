package org.oddjob.state;

import static org.oddjob.state.ParentState.*;

import org.oddjob.Structural;

/**
 * Implementation of a {@link StateOperator} that provides a parent state
 * as follows:
 * <ul>
 * <li>If any child is ACTIVE/EXECUTING then evaluate to ACTIVE.</li>
 * <li>If any child is EXCEPTION then evaluate to EXCEPTION.</li>
 * <li>If any child is INCOMPLETE then evaluate to INCOMPLETE.</li>
 * <li>If any child is READY then evaluate to READY.</li>
 * <li>Evaluate to COMPLETE.</li>
 * </ul>
 * 
 * This Operator is used in many {@link Structural}
 * jobs to calculate parent state.
 * 
 * @author rob
 *
 */
public class WorstStateOp implements StateOperator {
	
	private static final ParentState[][] STATE_MATRIX = {
		{ READY,      null, ACTIVE,     STARTED,    INCOMPLETE, READY,      EXCEPTION, null },
		{ null,       null, null,       null,       null,       null,       null,      null },
		{ ACTIVE,     null, ACTIVE,     ACTIVE,     INCOMPLETE, ACTIVE,     EXCEPTION, null },
		{ STARTED,    null, ACTIVE,     STARTED,    INCOMPLETE, STARTED,    EXCEPTION, null },
		{ INCOMPLETE, null, INCOMPLETE, INCOMPLETE, INCOMPLETE, INCOMPLETE, EXCEPTION, null },
		{ READY,      null, ACTIVE,     STARTED,    INCOMPLETE, COMPLETE,   EXCEPTION, null },
		{ EXCEPTION,  null, EXCEPTION,  EXCEPTION,  EXCEPTION, EXCEPTION,   EXCEPTION, null },
		{ null,       null, null,       null,       null,      null,        null,      null },
	};
	
	
	@Override
	public ParentState evaluate(State... states) {
		
		new AssertNonDestroyed().evaluate(states);
		
		ParentState state = ParentState.READY;
		
		if (states.length > 0) {
			
			state = new ParentStateConverter().toStructuralState(states[0]);
			
			for (int i = 1; i < states.length; ++i) {
				ParentState next = new ParentStateConverter().toStructuralState(
						states[i]);

				state = STATE_MATRIX[state.ordinal()][next.ordinal()];
			}
		}
		
		return state;
	}
	
}
