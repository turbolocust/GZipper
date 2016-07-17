/*
 * Copyright (C) 2016 Matthias
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gzipper.persistence;

import gzipper.domain.PersistenceFacade;
import gzipper.persistence.mappers.Mapper;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matthias Fussenegger
 */
public class PersistenceFacadeImpl implements PersistenceFacade {
    
    private final Map<Class, Mapper> _mappers = new HashMap<>();
    
    private PersistenceFacadeImpl() {
    }
    
    private Mapper getMapperForClass(Class<Mapper> mapperClass){
        
        Mapper mapper = null;
        
        try {
            mapper = _mappers.put(mapperClass, mapperClass.newInstance());
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(PersistenceFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return mapper;
    }
    
    public static PersistenceFacadeImpl getInstance() {
        return PersistenceFacadeImplHolder.INSTANCE;
    }
    
    private static class PersistenceFacadeImplHolder {

        private static final PersistenceFacadeImpl INSTANCE = new PersistenceFacadeImpl();
    }
}
