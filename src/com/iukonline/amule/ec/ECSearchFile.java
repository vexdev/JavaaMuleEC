package com.iukonline.amule.ec;

import java.util.Comparator;

import com.iukonline.amule.ec.exceptions.ECTagParsingException;


public class ECSearchFile {
    
    protected byte[] hash;           
    protected String fileName;        
    protected byte status;      
    protected int sourceCount;  
    protected int sourceXfer;   

    protected long sizeFull;       

    public ECSearchFile() {
        
    }
    
    public ECSearchFile(ECTag st) throws ECTagParsingException {
        this.updateFromECTag(st);
    }
    
    public void updateFromECTag(ECTag st) throws ECTagParsingException {
    }

    public byte[] getHash() {
        return hash;
    }

    public String getFileName() {
        return fileName;
    }

    public byte getStatus() {
        return status;
    }

    public int getSourceCount() {
        return sourceCount;
    }

    public int getSourceXfer() {
        return sourceXfer;
    }

    public long getSizeFull() {
        return sizeFull;
    }
    
    
    public static class ECSearchFileComparator implements Comparator<ECSearchFile> {
        
        // TODO Implement reverse y/n and let application decide
        
        public enum ComparatorType {
            SIZE, SOURCES_COUNT, SOURCES_XFER, FILENAME
        }
        
        private ComparatorType compType;

        
        public ECSearchFileComparator(ComparatorType compType) {
            this.compType = compType;
        }

        public ComparatorType getCompType() {
            return compType;
        }

        public void setCompType(ComparatorType compType) {
            this.compType = compType;
        }

        @Override
        public int compare(ECSearchFile object1, ECSearchFile object2) {
            switch (compType) {
            case SIZE:
                return (object2.getSizeFull() > object1.getSizeFull() ? 1 : object1.getSizeFull() == object2.getSizeFull() ? sortByFileName(object1, object2) : -1);
            case SOURCES_COUNT:
                return sortBySourcesCount(object1, object2);
            case SOURCES_XFER:
                return sortBySourcesCount(object1, object2);                
            case FILENAME:
            default:
                return sortByFileName(object1, object2);
            }
        }
        
        protected int sortBySourcesCount(ECSearchFile object1, ECSearchFile object2) {
            return (object2.getSourceCount() > object1.getSourceCount() ? 1 : object1.getSourceCount() == object2.getSourceCount() ? sortByFileName(object1, object2) : -1);
        }
        
        protected int sortBySourcesXfer(ECSearchFile object1, ECSearchFile object2) {
            return (object2.getSourceXfer() > object1.getSourceXfer() ? 1 : object1.getSourceXfer() == object2.getSourceXfer() ? sortByFileName(object1, object2) : -1);
        }
        
        protected int sortByFileName(ECSearchFile object1, ECSearchFile object2) {
            return object1.getFileName().compareToIgnoreCase(object2.getFileName());
        }
        
    }

    
    
}
