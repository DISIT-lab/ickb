/* Icaro Cloud Knowledge Base (ICKB).
   Copyright (C) 2015 DISIT Lab http://www.disit.org - University of Florence

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */

package it.cloudicaro.disit.kb;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author bellini
 */
@javax.ws.rs.ApplicationPath("api")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(it.cloudicaro.disit.kb.ApplicationTypeResource.class);
        resources.add(it.cloudicaro.disit.kb.ApplicationTypesResource.class);
        resources.add(it.cloudicaro.disit.kb.BusinessConfigurationCheckResource.class);
        resources.add(it.cloudicaro.disit.kb.BusinessConfigurationResource.class);        
        resources.add(it.cloudicaro.disit.kb.BusinessConfigurationsResource.class);        
        resources.add(it.cloudicaro.disit.kb.DataCenterResource.class);        
        resources.add(it.cloudicaro.disit.kb.DataCentersResource.class);        
        resources.add(it.cloudicaro.disit.kb.ServiceMetricResource.class);        
        resources.add(it.cloudicaro.disit.kb.StatusResource.class);        
        resources.add(it.cloudicaro.disit.kb.MetricTypeResource.class);        
        resources.add(it.cloudicaro.disit.kb.MetricTypesResource.class);        
    }
}
