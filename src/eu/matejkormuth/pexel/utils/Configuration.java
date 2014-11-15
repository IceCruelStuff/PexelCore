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
package eu.matejkormuth.pexel.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.matejkormuth.pexel.network.ServerType;

/**
 * Class that provides configuration.
 */
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration {
    protected transient Map<String, String> data  = new HashMap<String, String>();
    protected List<ConfigurationEntry>      entry = new ArrayList<ConfigurationEntry>();
    
    public Configuration() {
        
    }
    
    /**
     * Returns values by specified key.
     * 
     * @param key
     *            key
     * @return value
     */
    public String get(final String key) {
        return this.data.get(key);
    }
    
    public String getAsString(final String key) {
        return this.data.get(key).toString();
    }
    
    public int getAsInt(final String key) {
        return Integer.parseInt(this.data.get(key));
    }
    
    /**
     * Set's value by key.
     * 
     * @param key
     *            key
     * @param value
     *            value
     */
    public void set(final String key, final String value) {
        this.data.put(key, value);
    }
    
    /**
     * Saves this configuration to specified file.
     * 
     * @param file
     *            file to save configuration.
     */
    public void save(final File file) {
        for (String key : this.data.keySet()) {
            this.entry.add(new ConfigurationEntry(key, this.data.get(key)));
        }
        try {
            JAXBContext cont = JAXBContext.newInstance(Configuration.class);
            javax.xml.bind.Marshaller m = cont.createMarshaller();
            m.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(this, file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        this.entry.clear();
    }
    
    /**
     * Loads configuration from file.
     * 
     * @param file
     *            file
     * @return configuration
     */
    public static Configuration load(final File file) {
        Configuration conf = new Configuration();
        try {
            JAXBContext cont = JAXBContext.newInstance(Configuration.class);
            conf = (Configuration) cont.createUnmarshaller().unmarshal(file);
            
            for (ConfigurationEntry entry : conf.entry) {
                conf.data.put(entry.key, entry.value);
            }
            
            return conf;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
    
    @XmlType(name = "entry")
    protected static class ConfigurationEntry {
        public ConfigurationEntry() {
            
        }
        
        public ConfigurationEntry(final String key, final String value) {
            this.key = key;
            this.value = value;
        }
        
        @XmlAttribute(name = "key")
        public String key;
        @XmlAttribute(name = "value")
        public String value;
    }
    
    public static void createDefault(final ServerType type, final File f) {
        Configuration c = new Configuration();
        if (type == ServerType.MASTER) {
            c.set("authKey", "{insert 128 chars long auth key here}");
            c.set("port", "29631");
        }
        else {
            c.set("authKey", "{insert 128 chars long auth key here}");
            c.set("port", "29631");
            c.set("masterIp", "0.0.0.0");
        }
        
        c.save(f);
    }
}
