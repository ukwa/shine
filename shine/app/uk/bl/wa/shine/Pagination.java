package uk.bl.wa.shine;

import java.util.ArrayList;
import java.util.List;

public class Pagination {

    private int currentPage = 1;
    private int totalItems;
    private int itemsPerPage;

    private int totalPages = 0;

    public Pagination() {}
    
    public void update(int totalItems, int itemsPerPage, int pageNo) {

        this.totalItems = totalItems;
        this.itemsPerPage = itemsPerPage;
        if (this.itemsPerPage < 1) {
            this.itemsPerPage = 1;
        }

        this.totalPages = this.totalItems / this.itemsPerPage;
        if (this.totalItems % this.itemsPerPage > 0) {
            this.totalPages = this.totalPages + 1;
        }
        
        this.currentPage = pageNo;
    }

    public int getCurrentPage() {
        return currentPage;
    }
   
    public void setCurrentPage(int currentPage) {
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        if (currentPage < 1) {
            currentPage = 1;
        }
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return this.totalPages;
    }

    public boolean hasPreviousPage() {
        return currentPage > 1;
    }

    public boolean hasNextPage() {
        return currentPage < totalPages;
    }

    public int getPreviousPage() {
        if (hasPreviousPage()) {
            return currentPage - 1;
        } else {
            return 1;
        }
    }

    public int getNextPage() {
        if (hasNextPage()) {
            return currentPage + 1;
        } else {
            return totalPages;
        }
    }

    public int getStartIndex() {
        return (this.currentPage - 1) * this.itemsPerPage + 1;
    }
    
    public int getNextIndex(int currentIndex) {
    	return getStartIndex()+currentIndex;
    }

    public int getEndIndex() {
        int endIndex = this.currentPage * this.itemsPerPage;
        if (endIndex > this.totalItems) {
            endIndex = this.totalItems;
        }
        return endIndex;
    }

    public int getTotalItems() {
        return totalItems;
    }
    
    public List<Integer> getPagesList(int radius) {
        List<Integer> pageList = new ArrayList<Integer>();
        
        int startPage = getCurrentPage() - radius;
        if (startPage < 1) {
            startPage = 1;
        }
        
        int endPage = getCurrentPage() + radius;
        if (endPage > getTotalPages()) {
            endPage = getTotalPages();
        }
        
        for (int page = startPage; page <= endPage; page++) {
            pageList.add(page);
        }
        
        return pageList;
    }
    
	public String getDisplayXtoYofZ(String to, String of) {
        int first = this.getStartIndex();
        int last = this.getEndIndex();
        int total = this.getTotalItems();
          
        return first+to+last+of+total;
	}
}