package fdtmc;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FDTMC {

	private StateHandler stateHandler;
	private String variableName;
	private Map<State, List<Transition>> transitionSystem;
	private Map<String, List<Interface>> interfaces;


	public FDTMC() {
		stateHandler = new StateHandler(this);
		variableName = null;
		transitionSystem = new LinkedHashMap<State, List<Transition>>();
		interfaces = new LinkedHashMap<String, List<Interface>>();
	}

	public Collection<State> getStates() {
		return stateHandler.getStates();
	}

	public void setVariableName(String name) {
		variableName = name;
	}

	public String getVariableName() {
		return variableName;
	}

	public int getVariableIndex() {
		return stateHandler.getIndex();
	}

	public State createState() {
		return stateHandler.createState();
	}

	public State createState(String label) {
		return stateHandler.createState(label);
	}

    public State createInitialState() {
        return stateHandler.createInitialState();
    }

    private void setInitialState(State initialState) {
        stateHandler.setInitialState(initialState);
    }

    public State getInitialState() {
        return stateHandler.getInitialState();
    }
    
    public State createSuccessState() {
        return stateHandler.createSuccessState();
    }

    private void setSuccessState(State successState) {
        stateHandler.setSuccessState(successState);
    }

    public State getSuccessState() {
        return stateHandler.getSuccessState();
    }

    public State createErrorState() {
        return stateHandler.createErrorState();
    }

    private void setErrorState(State errorState) {
        stateHandler.setErrorState(errorState);
    }

    public State getErrorState() {
        return stateHandler.getErrorState();
    }

	public Transition createTransition(State source, State target, String action, String reliability) {
	    if (source == null) {
	        return null;
	    }

	    List<Transition> l = transitionSystem.get(source);
		if (l == null) {
			l = new LinkedList<Transition>();
		}

		Transition newTransition = new Transition(source, target, action, reliability);
		boolean success = l.add(newTransition);
		transitionSystem.put(source, l);
		return success ? newTransition : null;
	}

	/**
	 * Creates an explicit interface to another FDTMC.
	 *
	 * An interface is an FDTMC fragment with 3 states (initial, success, and error)
	 * and 2 transitions (initial to success with probability {@code id} and initial
	 * to error with probability 1 - {@code id}).
	 *
	 * @param id Identifier of the FDTMC to be abstracted away.
	 * @param initial Initial state of the interface.
	 * @param success Success state of the interface.
	 * @param error Error state of the interface.
	 */
	public Interface createInterface(String id, State initial, State success, State error) {
	    Transition successTransition = createTransition(initial, success, "", id);
	    Transition errorTransition = createTransition(initial, error, "", "1 - " + id);
	    Interface newInterface = new Interface(id,
	                                           initial,
	                                           success,
	                                           error,
	                                           successTransition,
	                                           errorTransition);

	    List<Interface> interfaceOccurrences = null;
	    if (interfaces.containsKey(id)) {
	        interfaceOccurrences = interfaces.get(id);
	    } else {
	        interfaceOccurrences = new LinkedList<Interface>();
	        interfaces.put(id, interfaceOccurrences);
	    }
	    interfaceOccurrences.add(newInterface);
	    return newInterface;
	}

	public State getStateByLabel(String label) {
		return stateHandler.getStateByLabel(label);
	}

	public Transition getTransitionByActionName(String action) {
		for (List<Transition> stateAdjacencies : transitionSystem.values()) {
			Transition stateAdjacency = findTransitionWithAction(action, stateAdjacencies);
			if (stateAdjacency != null)
				return stateAdjacency;
		}
		return null;
	}

	private Transition findTransitionWithAction(String action, List<Transition> stateAdjacencies) {
		for (Transition stateAdjacency : stateAdjacencies) {
			if (stateAdjacency.getActionName().equals(action))
				return stateAdjacency;
		}
		return null;
	}


	@Override
	public String toString() {
		String msg = new String();

		Set<State> tmpStates = this.transitionSystem.keySet();
		Iterator <State> itStates = tmpStates.iterator();
		while (itStates.hasNext()) {
			State temp = itStates.next();
			List<Transition> transitionList = this.transitionSystem.get(temp);
			if (transitionList != null) {
				Iterator <Transition> itTransitions = transitionList.iterator();
				while (itTransitions.hasNext()) {
					Transition t = itTransitions.next();
					msg += temp.getVariableName() + "=" + temp.getIndex() + ((temp.getLabel() != null) ? "(" + temp.getLabel() + ")" : "") +
							" --- " + t.getActionName() + " / " + t.getProbability() +
							" ---> " + t.getTarget().getVariableName() + "=" + t.getTarget().getIndex() + ((t.getTarget().getLabel() != null) ? "(" + t.getTarget().getLabel() + ")" : "") + "\n";
				}
			}
		}
		return msg;
	}

	/**
	 * Two FDTMCs are deemed equal whenever:
	 *     - their states are equal;
	 *     - their initial, success, and error states are equal;
	 *     - the transitions with concrete values are equal;
	 *     - the transitions with variable names have equal source and target states; and
	 *     - the abstracted interfaces are equal.
	 */
	@Override
	public boolean equals(Object obj) {
	    if (obj != null && obj instanceof FDTMC) {
            return areStatesTransitionAndInterfacesEquals((FDTMC) obj);
	    }
	    return false;
	}
	
	private boolean areStatesTransitionAndInterfacesEquals(FDTMC other) {
		boolean areStatesTransitionAndInterfacesEquals = true;
		
		if (areStatesAndTransitionSystemEquals(other)) {
			LinkedList<List<Interface>> thisInterfaces = new LinkedList<List<Interface>>(interfaces.values());
			LinkedList<List<Interface>> otherInterfaces = new LinkedList<List<Interface>>(other.interfaces.values());
			if (!thisInterfaces.equals(otherInterfaces)) {
				areStatesTransitionAndInterfacesEquals = false;
			}
		} else {
			areStatesTransitionAndInterfacesEquals = false;
		}
		return areStatesTransitionAndInterfacesEquals;
	}
	
	private boolean areStatesAndTransitionSystemEquals(FDTMC other) {
		boolean areStatesEquals = stateHandler.getStates().equals(other.stateHandler.getStates())
				&& getInitialState().equals(other.getInitialState())
				&& getSuccessState().equals(other.getSuccessState())
				&& getErrorState().equals(other.getErrorState());
		
		return areStatesEquals && transitionSystem.equals(other.transitionSystem);
	}

	@Override
    public int hashCode() {
        return stateHandler.getStates().hashCode() + transitionSystem.hashCode() + interfaces.hashCode();
    }

    public Map<State, List<Transition>> getTransitions() {
		return transitionSystem;
	}

	/**
	 * Inlines the given FDTMCs whenever there is an interface corresponding
	 * to the string in the respective index.
	 *
	 * @param indexedModels
	 * @return a new FDTMC which represents this one with the ones specified
	 *         in {@code indexedModels} inlined.
	 */
    public FDTMC inline(Map<String, FDTMC> indexedModels) {
        FDTMC inlined = new FDTMC();
        
        Map<State, State> statesMapping = inlined.getCopyForInlining(this);

        for (Map.Entry<String, List<Interface>> entry: interfaces.entrySet()) {
            String dependencyId = entry.getKey();
            if (indexedModels.containsKey(dependencyId)) {
                FDTMC fragment = indexedModels.get(dependencyId);
                for (Interface iface: entry.getValue()) {
                    inlined.inlineInInterface(iface,
                                              fragment,
                                              statesMapping);
                }
            }
        }
        return inlined;
    }

	private Map<State, State> getCopyForInlining(FDTMC origin) {
		variableName = origin.getVariableName();

        Map<State, State> statesMapping = inlineStates(origin);
        setInitialState(statesMapping.get(origin.getInitialState()));
        setSuccessState(statesMapping.get(origin.getSuccessState()));
        setErrorState(statesMapping.get(origin.getErrorState()));

        inlineTransitions(origin, statesMapping);
		return statesMapping;
	}

    /**
     * Returns a copy of this FDTMC decorated with "presence transitions",
     * i.e., a new initial state with a transition to the original initial
     * state parameterized by the {@code presenceVariable} and a complement
     * transition ({@code 1 - presenceVariable}) to the success state
     * ("short-circuit").
     *
     * @param presenceVariable
     * @return
     */
    public FDTMC decoratedWithPresence(String presenceVariable) {
        FDTMC decorated = copy();

        // Enter the original chain in case of presence
        decorated.createTransition(decorated.createInitialState(),
        		 				   decorated.getInitialState(),
                                   "",
                                   presenceVariable);
        // Short-circuit in case of absence
        decorated.createTransition(decorated.createInitialState(),
                                   decorated.getSuccessState(),
                                   "",
                                   "1-"+presenceVariable);
        return decorated;
    }

    /**
     * Returns an FDTMC with a transition to {@code ifPresent} annotated by
     * {@code presenceVariable} and a complement one ({@code 1 - ifPresent})
     * to {@code ifAbsent}. Of course, {@code presenceVariable} is meant to
     * be resolved with a value of 0 or 1.
     *
     * The success states of both {@code ifPresent} and {@code ifAbsent} are
     * linked to a new success state.
     *
     * @param presenceVariable
     * @param ifPresent
     * @param ifAbsent
     * @return
     */
    public static FDTMC ifThenElse(String presenceVariable, FDTMC ifPresent, FDTMC ifAbsent) {
        // TODO Handle ifAbsent.
        return ifPresent.decoratedWithPresence(presenceVariable);
    }

    /**
     * Copies this FDTMC.
     * @return a new FDTMC which is a copy of this one.
     */
    private FDTMC copy() {
        FDTMC copied = new FDTMC();
        Map<State, State> statesMapping = getCopyForInlining(copied);
        copied.inlineInterfaces(this, statesMapping);
        return copied;
    }

    /**
     * Inlines all states from {@code fdtmc} stripped of their labels.
     * @param fdtmc
     * @return
     */
    private Map<State, State> inlineStates(FDTMC fdtmc) {
        Map<State, State> statesOldToNew = new HashMap<State, State>();
        for (State state: fdtmc.getStates()) {
            State newState = this.createState();
            statesOldToNew.put(state, newState);
        }
        return statesOldToNew;
    }

    /**
     * Inlines all transitions from {@code fdtmc} that are not part of an interface.
     *
     * @param fdtmc
     * @param statesOldToNew
     */
    private void inlineTransitions(FDTMC fdtmc, Map<State, State> statesOldToNew) {
        Set<Transition> interfaceTransitions = fdtmc.getInterfaceTransitions();
        for (Map.Entry<State, List<Transition>> entry : fdtmc.getTransitions().entrySet()) {
            List<Transition> transitions = entry.getValue();
            if (transitions != null) {
                for (Transition transition : transitions) {
                    if (!interfaceTransitions.contains(transition)) {
                        inlineTransition(transition, statesOldToNew);
                    }
                }
            }
        }
    }

    private Transition inlineTransition(Transition transition, Map<State, State> statesOldToNew) {

        return createTransition(statesOldToNew.get(transition.getSource()),
        						statesOldToNew.get(transition.getTarget()),
                                transition.getActionName(),
                                transition.getProbability());
    }

    /**
     * Inlines all interfaces (and respective transitions) from {@code fdtmc}
     * into this one.
     *
     * @param fdtmc
     * @param statesOldToNew
     */
    private void inlineInterfaces(FDTMC fdtmc, Map<State, State> statesOldToNew) {
        for (Map.Entry<String, List<Interface>> entry : fdtmc.interfaces.entrySet()) {
            List<Interface> newInterfaces = new LinkedList<Interface>();
            this.interfaces.put(entry.getKey(), newInterfaces);
            for (Interface iface : entry.getValue()) {
                Transition successTransition = inlineTransition(iface.getSuccessTransition(), statesOldToNew);
                Transition errorTransition = inlineTransition(iface.getErrorTransition(), statesOldToNew);
                Interface newInterface = new Interface(iface.getAbstractedId(),
                                                       statesOldToNew.get(iface.getInitial()),
                                                       statesOldToNew.get(iface.getSuccess()),
                                                       statesOldToNew.get(iface.getError()),
                                                       successTransition,
                                                       errorTransition);
                newInterfaces.add(newInterface);
            }
        }
    }

    private void inlineInInterface(Interface iface, FDTMC fragment, Map<State, State> statesMapping) {
        Map<State, State> fragmentStatesMapping = this.inlineStates(fragment);
        this.inlineTransitions(fragment, fragmentStatesMapping);

        State initialInlined = iface.getInitial();
        State initialFragment = fragment.getInitialState();
        State successInlined = iface.getSuccess();
        State successFragment = fragment.getSuccessState();
        State errorInlined = iface.getError();
        State errorFragment = fragment.getErrorState();

        this.createTransition(statesMapping.get(initialInlined),
                              fragmentStatesMapping.get(initialFragment),
                              "",
                              "1");
        this.createTransition(fragmentStatesMapping.get(successFragment),
                              statesMapping.get(successInlined),
                              "",
                              "1");
        if (errorFragment != null) {
            this.createTransition(fragmentStatesMapping.get(errorFragment),
                                  statesMapping.get(errorInlined),
                                  "",
                                  "1");
        }
    }

    private Set<Transition> getInterfaceTransitions() {
        Set<Transition> transitions = new HashSet<Transition>();
        interfaces.values().stream().flatMap(List<Interface>::stream)
                .forEach(iface -> {
                    transitions.add(iface.getSuccessTransition());
                    transitions.add(iface.getErrorTransition());
                });
        return transitions;
    }

}
