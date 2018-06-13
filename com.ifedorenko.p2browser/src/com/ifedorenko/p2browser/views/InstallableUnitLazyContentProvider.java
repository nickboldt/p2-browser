/*******************************************************************************
 * Copyright (c) 2011 Igor Fedorenko
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Igor Fedorenko - initial API and implementation
 *******************************************************************************/

package com.ifedorenko.p2browser.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.ifedorenko.p2browser.model.IGroupedInstallableUnits;

abstract class InstallableUnitLazyContentProvider
    implements ILazyTreeContentProvider
{
    private final TreeViewer treeViewer;

    private Map<IInstallableUnit, InstallableUnitNode> nodes;

    public InstallableUnitLazyContentProvider( TreeViewer treeViewer )
    {
        this.treeViewer = treeViewer;
    }

    @Override
    public void dispose()
    {
    }

    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        nodes = new HashMap<IInstallableUnit, InstallableUnitNode>();
    }

    protected Object[] getChildren( Object parentElement )
    {
        if ( parentElement instanceof IGroupedInstallableUnits )
        {
            IGroupedInstallableUnits metadata = (IGroupedInstallableUnits) parentElement;
            return toViewNodes( metadata, metadata.getRootIncludedInstallableUnits() );
        }
        else if ( parentElement instanceof InstallableUnitNode )
        {
            InstallableUnitNode node = (InstallableUnitNode) parentElement;
            IGroupedInstallableUnits metadata = node.getMetadata();
            return toViewNodes( metadata, metadata.getIncludedInstallableUnits( node.getInstallableUnit(), false ) );
        }
        return null;
    }

    private HashMap<Object, Object[]> cachedChildren;
    private HashMap<Object, Long> cachedExpiration;
    private Object[] getCachedChildren(Object parentElement, long duration) {
    	if( cachedChildren == null ) {
    		cachedChildren = new HashMap<Object, Object[]>();
    	}
    	if( cachedExpiration == null ) {
    		cachedExpiration = new HashMap<Object, Long>();
    	}
    	if( cachedExpiration.get(parentElement) == null 
    			|| cachedExpiration.get(parentElement).longValue() < System.currentTimeMillis() 
    			|| cachedChildren.get(parentElement) == null ) {
    		Object[] elements = getChildren( parentElement );
    		cachedChildren.put(parentElement, elements);
    		cachedExpiration.put(parentElement, System.currentTimeMillis()+duration);
    		return elements;
    	}
    	return cachedChildren.get(parentElement);
    }
    
    
    @Override
    public void updateElement( Object parentElement, int index )
    {
        Object[] elements = getCachedChildren( parentElement, 20000 );
        if ( elements != null && elements.length > index )
        {
            Object element = elements[index];
            treeViewer.replace( parentElement, index, element );

            Object[] grandChildren = getCachedChildren( element , 20000);
            if ( grandChildren != null )
            {
                treeViewer.setChildCount( element, grandChildren.length );
            }
        }
    }

    @Override
    public void updateChildCount( Object element, int currentChildCount )
    {
        Object[] elements = getChildren( element );
        if ( elements != null && elements.length != currentChildCount )
        {
            treeViewer.setChildCount( element, elements.length );
        }
    }

    @Override
    public Object getParent( Object element )
    {
        return null;
    }

    protected Object[] toViewNodes( IGroupedInstallableUnits metadata, Collection<IInstallableUnit> units )
    {
        ArrayList<InstallableUnitNode> nodes = new ArrayList<InstallableUnitNode>();
        for ( IInstallableUnit unit : units )
        {
            InstallableUnitNode node = this.nodes.get( unit );
            if ( node == null )
            {
                node = new InstallableUnitNode( metadata, unit );
                this.nodes.put( unit, node );
            }
            nodes.add( node );
        }
        return nodes.toArray();
    }

}
