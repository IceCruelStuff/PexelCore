// @formatter:off
/*
 * Pexel Project - Minecraft minigame server platform. 
 * Copyright (C) 2014 Matej Kormuth <http://www.matejkormuth.eu>
 * 
 * This file is part of Pexel.
 * 
 * Pexel is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Pexel is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
// @formatter:on
package eu.matejkormuth.pexel.PexelCore.arenas;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.bukkit.Location;

import eu.matejkormuth.pexel.PexelCore.core.Region;
import eu.matejkormuth.pexel.PexelCore.core.RegionTransformer;
import eu.matejkormuth.pexel.PexelCore.util.SerializableLocation;

/**
 * Class that represents playable map.
 * 
 * @author Mato Kormuth
 * 
 */
@XmlType(name = "arenamap")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class MapData {
    @XmlAttribute(name = "name")
    protected String                                  name;
    @XmlAttribute(name = "minigameName")
    protected String                                  minigameName;
    @XmlAttribute(name = "author")
    protected String                                  author;
    
    @XmlElementWrapper(name = "options")
    protected final Map<String, String>               options       = new HashMap<String, String>();
    @XmlElementWrapper(name = "locations")
    protected final Map<String, SerializableLocation> locations     = new HashMap<String, SerializableLocation>();
    @XmlElementWrapper(name = "regions")
    protected final Map<String, Region>               regions       = new HashMap<String, Region>();
    
    @XmlAttribute(name = "locationType")
    protected LocationType                            locationsType = LocationType.ABSOLUTE;
    
    @XmlAttribute(name = "anchor")
    // Used only if locationsType is RELATIVE.
    protected SerializableLocation                    anchor        = null;
    
    public static final MapData load(final File file) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(MapData.class);
        Unmarshaller un = jc.createUnmarshaller();
        return (MapData) un.unmarshal(file);
    }
    
    public void save(final File file) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(MapData.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(this, file);
    }
    
    public boolean validate(final SimpleArena arena) {
        return arena.getMinigame().getName().equals(this.minigameName);
    }
    
    public String getOption(final String key) {
        return this.options.get(key);
    }
    
    public String getMinigameName() {
        return this.minigameName;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getAuthor() {
        return this.author;
    }
    
    public Location getLocation(final String key) {
        if (this.locationsType == LocationType.ABSOLUTE) {
            return this.locations.get(key).getLocation();
        }
        else {
            if (this.anchor != null) {
                return this.anchor.getLocation().add(
                        this.locations.get(key).getLocation());
            }
            else {
                throw new InvalidMapDataException(
                        "Can't return relatiive location when anchor is null.");
            }
        }
    }
    
    public Region getRegion(final String key) {
        if (this.locationsType == LocationType.ABSOLUTE) {
            return this.regions.get(key);
        }
        else {
            return RegionTransformer.toAbsolute(this.regions.get(key),
                    this.anchor.getLocation());
        }
    }
    
    public Map<String, String> getOptions() {
        return this.options;
    }
    
    public Map<String, SerializableLocation> getLocations() {
        return this.locations;
    }
    
    public Map<String, Region> getRegions() {
        return this.regions;
    }
}
