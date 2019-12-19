package org.usfirst.frc.tm744yr18.robot.interfaces;

public interface TmItemAvailabilityI {
	public static enum ItemAvailabilityE { ACTIVE, USE_FAKE, DISABLED, NOT_INSTALLED, SEE_NAMED_CONTROL;
		
		public boolean isRunable() {
			boolean ans;
			switch(this) {
			case ACTIVE:
			case USE_FAKE:
			case SEE_NAMED_CONTROL:
				ans = true;
				break;
			case DISABLED:
			case NOT_INSTALLED:
			default:
				ans = false;
			}
			return ans;
		}
	}
	
	public static enum ItemFakeableE { FAKEABLE, NOT_FAKEABLE;
		
		public boolean isFakeable() { return this.equals(FAKEABLE); }
	}
	
	public boolean isFakeableItem();
	public void configAsFake();
	public boolean isFake();
}
