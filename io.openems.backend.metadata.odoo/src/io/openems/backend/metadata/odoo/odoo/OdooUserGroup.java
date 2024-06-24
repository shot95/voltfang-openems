package io.openems.backend.metadata.odoo.odoo;

public enum OdooUserGroup {

	// XXX PORTAL id might differ between different Odoo databases (default value on Odoo 16 is 10 not 65)
	PORTAL(10); // oEMS

	private final int groupId;

	private OdooUserGroup(int groupId) {
		this.groupId = groupId;
	}

	public int getGroupId() {
		return this.groupId;
	}

}
