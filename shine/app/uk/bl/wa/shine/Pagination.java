package uk.bl.wa.shine;

import java.util.ArrayList;
import java.util.List;

public class Pagination {

    private int currentPage = 1;
    private int totalItems;
    private int itemsPerPage;
    private int maxNumberOfLinksOnPage; // i.e 10 links per page
    private int maxViewablePages; // i.e 50 pages max

    private int totalPages = 0;

    // The paging must limit the depth to which we allow the user to descend, as otherwise Solr starts to fall over. 
    // The Pager should only show ten page numbers at once, and should only allow 50 pages to be viewed. 
    // On the last page of results, you should instead get a warning that you can't page any deeper.

    public Pagination(int itemsPerPage, int maxNumberOfLinksOnPage, int maxViewablePages) {
    	this.itemsPerPage = itemsPerPage;
    	this.maxNumberOfLinksOnPage = maxNumberOfLinksOnPage; // 10
    	this.maxViewablePages = maxViewablePages; // 500
        if (this.itemsPerPage < 1) {
            this.itemsPerPage = 1;
        }
    }
    
    public void update(int totalItems, int pageNo) {

        this.totalItems = totalItems;
        // Place hard upper limit on paging
        this.totalPages = this.totalItems / this.itemsPerPage;
        if (this.totalItems % this.itemsPerPage > 0) {
            this.totalPages = this.totalPages + 1;
        }
        if (this.totalPages > maxViewablePages) this.totalPages = maxViewablePages;
        
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
    
    public List<Integer> getPagesList() {
    	int radius = this.maxNumberOfLinksOnPage / 2;
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

	public int getMaxNumberOfLinksOnPage() {
		return maxNumberOfLinksOnPage;
	}

	public int getMaxViewablePages() {
		return maxViewablePages;
	}
	
	public boolean hasMaxViewablePagedReached() {
		return (this.currentPage == this.maxViewablePages);
	}
	
	
}