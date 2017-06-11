package switchingQueue;

import desmoj.core.simulator.*;
import singleQueue.Customer;

public class CustomerFinishedEvent extends EventOf2Entities<Customer, VendingMachine>{
	
	private VendingMachineModelScenario3 model;

	public CustomerFinishedEvent(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);
		
		model = (VendingMachineModelScenario3) owner;
	}

	@Override
	public void eventRoutine(Customer customer, VendingMachine vendingMachine) {
		
		model.sendTraceNote("Customer: " + customer + " finished.");
		
		// allow last customers to switch machines if queue would be shorter
		while(switchPossible()){
			
		}

		// get the queue from the machine of the finished customer
		Queue<Customer> customerQueue = vendingMachine.getCustomerQueue();
		
		// queue not empty, handle next customer
		if(!customerQueue.isEmpty()) {
			Customer nextCustomer = customerQueue.first();
			customerQueue.remove(nextCustomer);
			nextCustomer.setDequeue(model.presentTime());
			model.customers.add(nextCustomer);
			
			CustomerFinishedEvent customerFinished = new CustomerFinishedEvent(model, "CustomerFinished", true);
			
			customerFinished.schedule(nextCustomer, vendingMachine, new TimeSpan(model.getCustomerDuration()));
		}
		// queue empty, set machine to free
		else{
			vendingMachine.setState(0);
		}
		
	}
	
	private void switchCustomer(VendingMachine from, VendingMachine to) {
		model.sendTraceNote("Last customer switched from " + from + " Q: " + from.getCustomerQueue().size() + " to " + to + " Q: " + to.getCustomerQueue().size());
		Customer fromCustomer = from.getCustomerQueue().last();
		from.getCustomerQueue().remove(fromCustomer);
		to.getCustomerQueue().insert(fromCustomer);
		if(to.state == 0){
			to.setState(1);
			// remove customer
			Customer customer = to.getCustomerQueue().first();
			to.getCustomerQueue().remove(customer);
			customer.setDequeue(model.presentTime());
			model.customers.add(customer);
			
			CustomerFinishedEvent customerFinished = new CustomerFinishedEvent(model, "CustomerFinished", true);
			customerFinished.schedule(customer, to, new TimeSpan(model.getCustomerDuration()));
		}
	}
	
	private boolean switchPossible() {
		boolean switched = false;
		int numberOfMachines = model.vendingMachines.size();
		for(int i=0; i<numberOfMachines; i++){
			if (switched)
				break;
			VendingMachine machineI = model.vendingMachines.get(i);
			for(int j=i+1; j<numberOfMachines; j++){
				VendingMachine machineJ = model.vendingMachines.get(j);
				if(machineI.getCustomerQueue().size()-1 > machineJ.getCustomerQueue().size()) {
					switchCustomer(machineI, machineJ);
					switched = true;
					break;
				} else if(machineJ.getCustomerQueue().size()-1 > machineI.getCustomerQueue().size()){
					switchCustomer(machineJ,machineI);
					switched = true;
					break;
				}
			}
		}
		return switched;
	}
	
}
