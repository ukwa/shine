package uk.bl.wa.shine;

public class Pagination {

    private int currentPage = 1;
    private int totalItems;
    private int itemsPerPage;

    private int totalPages = 0;
    private int currentPageSize;

    public Pagination() {}
    
    public void update(int totalItems, int itemsPerPage, int pageNo, int currentPageSize) {

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
        this.currentPageSize = currentPageSize;

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
    
	public String getDisplayXtoYofZ(String to, String of) {
        int first = (currentPage - 1) * itemsPerPage + 1;
        int last = first + currentPageSize-1;
        int total = totalItems;
          
        return first+to+last+of+total;
	}
}