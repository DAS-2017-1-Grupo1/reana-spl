package parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import FeatureFamilyBasedAnalysisTool.FDTMC;
import FeatureFamilyBasedAnalysisTool.State;

public class DiagramAPI {
	private final File xmlFile;
	private ArrayList<SDReader> sdParsers;
	private ArrayList<ADReader> adParsers;
	private ArrayList<FDTMC> fdtmcs;
	private HashMap<String, Fragment> sdByID;
	private HashMap<String, FDTMC> fdtmcByName;

	public DiagramAPI(File xmlFile) {
		this.xmlFile = xmlFile;
		adParsers = new ArrayList<ADReader>();
		sdParsers = new ArrayList<SDReader>();
		sdByID = new HashMap<String, Fragment>();
		fdtmcByName = new HashMap<String, FDTMC>();
	}

	public void initialize() throws InvalidTagException, UnsupportedFragmentTypeException {

		ADReader adParser = new ADReader(this.xmlFile, 0);
		adParser.retrieveActivities();
		this.adParsers.add(adParser);

		boolean hasNext = false;
		int index = 0;
		do {
			SDReader sdParser = new SDReader(this.xmlFile, index);
			sdParser.retrieveLifelines();
			sdParser.retrieveMessages();
			sdParser.traceDiagram();
			sdByID.put(sdParser.getSd().getId(), sdParser.getSd());
			this.sdParsers.add(sdParser);
			hasNext = sdParser.hasNext();
			index++;
		} while (hasNext);
		linkSdToActivity(this.adParsers.get(0));

		/*adParser.printAll();
		for (SDReader sdp : this.sdParsers) {
			sdp.printAll();
		}*/
	}

	public void linkSdToActivity(ADReader ad) {
		for (Activity a : ad.getActivities()) {
			if (a.getSdID() != null) {
				a.setSd(sdByID.get(a.getSdID()));
			}
		}
	}
	
	public void transform () {
		for (ADReader adParser : this.adParsers) {
			transformSingleAD(adParser);
		}
		
		for (SDReader sdParser : this.sdParsers) {
			transformSingleSD(sdParser.getSd());
		}
	}
	
	public void transformSingleAD (ADReader adParser) {
		
	}
	
	public void transformSingleSD (Fragment fragment) {
		FDTMC fdtmc = new FDTMC();
		State init, error, success, source, target, featStart;
		
		if (fragment.getOperandName() != null) fdtmc.setVariableName("s" + fragment.getOperandName());
		else fdtmc.setVariableName("s" + fragment.getName());
		//adiciona a lista e poe no hashmap:
		//fdtmcByName.put(key, fdtmc);
		
		error = fdtmc.createState("error");
		init = fdtmc.createState("init");
		source = init;
		
		int i = 1;
		for (Node n : fragment.getNodes()) {
			if (i++ == fragment.getNodes().size()) {
				success = fdtmc.createState("success");
				if (n.getClass().equals(Message.class)) {
					fdtmc.createTransition(source, success, "r" + ((Message) n).getReceiver().getName());
					fdtmc.createTransition(source, error, "1-r" + ((Message) n).getReceiver().getName());
				} else if (n.getClass().equals(Fragment.class)) {
					featStart = fdtmc.createState("start" + ((Fragment) n).getOperandName());
					fdtmc.createTransition(source, featStart, "f" + ((Fragment) n).getOperandName());
					fdtmc.createTransition(source, success, "1-f" + ((Fragment) n).getOperandName());
					transformSingleSD ((Fragment) n);
				}
				
		    } else {
		    	if (n.getClass().equals(Message.class)) {
					target = fdtmc.createState();
					fdtmc.createTransition(source, target, "r" + ((Message) n).getReceiver().getName());
					fdtmc.createTransition(source, error, "1-r" + ((Message) n).getReceiver().getName());
					source = target;
				} else if (n.getClass().equals(Fragment.class)) {
					featStart = fdtmc.createState("start" + ((Fragment) n).getOperandName());
					target = fdtmc.createState();
					fdtmc.createTransition(source, featStart, "f" + ((Fragment) n).getOperandName());
					fdtmc.createTransition(source, target, "1-f" + ((Fragment) n).getOperandName());
					transformSingleSD ((Fragment) n);
					source = target;
				}
		    }
		}
		System.out.println(fdtmc.toString());
	}
}