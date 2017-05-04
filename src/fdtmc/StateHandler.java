package fdtmc;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class StateHandler {

	private static final String INITIAL_LABEL = "initial";
	private static final String SUCCESS_LABEL = "success";
	private static final String ERROR_LABEL = "error";

	private final FDTMC fdtmc;

	private int index;

	private Set<State> states;
	private State initialState;
	private State successState;
	private State errorState;

	public StateHandler(FDTMC fdtmc) {
		this.fdtmc = fdtmc;
		states = new LinkedHashSet<State>();
		initialState = null;
		index = 0;
	}

	public State createState() {
		State temp = new State();
		temp.setVariableName(fdtmc.getVariableName());
		temp.setIndex(fdtmc.getVariableIndex());
		states.add(temp);
		fdtmc.getTransitions().put(temp, null);
		if (index == 0)
			setInitialState(temp);
		index++;
		return temp;
	}

	public State createState(String label) {
		State temp = createState();
		temp.setLabel(label);
		return temp;
	}

	public int getIndex() {
		return index;
	}

	public Set<State> getStates() {
		return states;
	}

	public void setStates(Set<State> states) {
		this.states = states;
	}

	public State createInitialState() {
		State initial = createState();
		setInitialState(initial);
		return initial;
	}

	public State getInitialState() {
		return initialState;
	}

	public void setInitialState(State initialState) {
		if (this.initialState != null) {
			this.initialState.setLabel(null);
		}
		this.initialState = initialState;
		this.initialState.setLabel(INITIAL_LABEL);
	}

	public State createSuccessState() {
		State success = createState();
		setSuccessState(success);
		return success;
	}

	public State getSuccessState() {
		return successState;
	}

	public void setSuccessState(State successState) {
		this.successState = successState;
		this.successState.setLabel(SUCCESS_LABEL);
	}

	public State createErrorState() {
		State error = createState();
		setErrorState(error);
		return error;
	}

	public State getErrorState() {
		return errorState;
	}

	public void setErrorState(State errorState) {
		this.errorState = errorState;
		this.errorState.setLabel(ERROR_LABEL);
	}

	public State getStateByLabel(String label) {
		Iterator <State> it = states.iterator();
		while (it.hasNext()){
			State s = it.next();
			if (s.getLabel().equals(label))
				return s;
		}
		return null;
	}
}