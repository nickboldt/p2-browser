/*******************************************************************************
 * Copyright (c) 2009, 2017 IBM Corporation and others.
 *
 * This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package copied.org.eclipse.equinox.internal.p2.director;

import java.util.Map;
import org.eclipse.equinox.internal.p2.metadata.RequiredCapability;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.query.IQueryable;

@SuppressWarnings("restriction")
public class PermissiveSlicer extends Slicer {
	private boolean includeOptionalDependencies; //Cause optional dependencies not be followed as part of the
	private boolean everythingGreedy;
	private boolean considerFilter;
	private boolean considerOnlyStrictDependency;
	private boolean evalFilterTo;
	private boolean onlyFilteredRequirements;

	public PermissiveSlicer(IQueryable<IInstallableUnit> input, Map<String, String> context, boolean includeOptionalDependencies, boolean everythingGreedy, boolean evalFilterTo, boolean considerOnlyStrictDependency, boolean onlyFilteredRequirements) {
		super(input, context, true);
		this.considerFilter = (context != null && context.size() > 1) ? true : false;
		this.includeOptionalDependencies = includeOptionalDependencies;
		this.everythingGreedy = everythingGreedy;
		this.evalFilterTo = evalFilterTo;
		this.considerOnlyStrictDependency = considerOnlyStrictDependency;
		this.onlyFilteredRequirements = onlyFilteredRequirements;
	}

	@Override
	protected boolean isApplicable(IInstallableUnit iu) {
		if (considerFilter)
			return super.isApplicable(iu);
		if (iu.getFilter() == null)
			return true;
		return evalFilterTo;
	}

	@Override
	protected boolean isApplicable(IRequirement req) {
		//Every filter in this method needs to continue except when the filter does not pass
		if (!includeOptionalDependencies)
			if (req.getMin() == 0)
				return false;

		if (considerOnlyStrictDependency) {
			if (!RequiredCapability.isStrictVersionRequirement(req.getMatches()))
				return false;
		}

		//deal with filters
		if (considerFilter) {
			if (onlyFilteredRequirements && req.getFilter() == null) {
				return false;
			}
			return super.isApplicable(req);
		}
		if (req.getFilter() == null) {
			if (onlyFilteredRequirements)
				return false;
			return true;
		}
		return evalFilterTo;
	}

	@Override
	protected boolean isGreedy(IRequirement req) {
		if (everythingGreedy) {
			return true;
		}
		return super.isGreedy(req);
	}
}
