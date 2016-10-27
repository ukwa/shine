$(function () {
	
	$("[data-toggle=tooltip]").tooltip();
	
	$('#query').keypress(function(event){
		var keycode = (event.keyCode ? event.keyCode : event.which);
		if(keycode == '13'){
		    $('#search').click();
		}
	});
	
	$('#reset').click(function(event) {
		$('.search-field').each(function() {
			$(this).attr('value', ''); 
		});
		//clearFacets();
	});
	
	$('.paging').click(function() {
		if ($('#search-form').valid()) {
		    $('#modalLoader').modal({
		        backdrop: true,
		        keyboard: true
		    });
		}
	});
	
	$('#sort').change(function(event) {
		event.preventDefault();
		if ($('#search-form').valid()) {
		    $('#modalLoader').modal({
		        backdrop: true,
		        keyboard: true
		    });
			$('#search-form').submit();
		}
	});

	$('#order').change(function(event) {
		console.log('order');
		event.preventDefault();
		if ($('#search-form').valid()) {
		    $('#modalLoader').modal({
		        backdrop: true,
		        keyboard: true
		    });
			$('#search-form').submit();
		}
	});

	$(".facet-search").each(function(index) {
		$(this).click(function(event) {
			event.preventDefault();
//			console.log(index);
			var addMoreSelector = "#add-more-option-" + index;
			$(addMoreSelector).removeClass('hide');
		});
	});
	
	$(".close-facet-value").each(function(index) {
		$(this).click(function(event) {
			event.preventDefault();
			console.log("closing: " + index);
			var addMoreSelector = "#add-more-option-" + index;
			$(addMoreSelector).addClass('hide');
		});
	});
	

	
	if ($('.max-viewable-reached').attr('class') != undefined) {
		$('#max-view-reached').removeClass('hide');
	}
	
	// read in what was submitted
	$facet_sort_input = $('#facet-sort');
	$link_sort_count = $('#facet-sort-count');
	$link_sort_value = $('#facet-sort-value');

	if ($facet_sort_input.attr('checked') === undefined) {
		$(".facet-sort").each(function(index) {
			// unchecked then reset all buttons
			var $link_span = $(this);
			$link_span.removeClass('btn-success');
			$link_span.addClass('btn-primary');
		});
	} else {
		if ($facet_sort_input.val() == 'count') {
			$link_sort_count.removeClass('btn-primary');
			$link_sort_count.addClass('btn-success');
		} else if ($facet_sort_input.val() == 'index') {
			$link_sort_value.removeClass('btn-primary');
			$link_sort_value.addClass('btn-success');
		}
	}

	$('.facet-sort-count').each(function(index) {
		
	});
	$('.facet-sort-value').each(function(index) {
		
	});

	
	// on clicks
	$("button.facet-sort").each(function(index) {
		var $link_span = $(this);
		$(this).click(function(event) {
			event.preventDefault();
			if ($('#search-form').valid()) {
			    $('#modalLoader').modal({
			        backdrop: true,
			        keyboard: true
			    });
			}
			if ($link_span.hasClass('btn-primary')) {
				// SELECTED
				$link_span.removeClass('btn-primary');
				$link_span.addClass('btn-success');
				if ($link_span.val() == 'count') {
					$facet_sort_input.val("count");
				} else if ($link_span.val() == 'value') {
					$facet_sort_input.val("index");
				}
				$facet_sort_input.attr("checked", "checked");
			} else {
				// DESELECTED
				$link_span.removeClass('btn-success');
				$link_span.addClass('btn-primary');
				$facet_sort_input.val("");
				$facet_sort_input.removeAttr("checked");
			}
 			$('#search-form').submit();
		});
		
	});
	
	var inList = [];
	
	$(".add-facet-button").each(function() {
		$(this).click(function(event) {
			event.preventDefault();
			var $search_field = $(this).parent().parent().find('input.form-control.add-facet-field')
	    	searchFacetValues($search_field);
		});
	});
	
	$('input.add-facet-field').each(function() {
	    $(this).keyup(function() {
	    	searchFacetValues($(this));
	    });
	});

	searchFacetValues = function($element) {
		var $facet_index = $element.parent().parent();
		if ($facet_index.find('span.tt-dropdown-menu').css('display') == 'none') {
			var $search_field = $element;
			var $hidden_facets = $facet_index.find('ul.list-unstyled li.facet-options.hide');
			$hidden_facets.each(function(index) {
				var $value = $(this).find('a:nth-child(3)');
				var $copied_value = $value.clone();
				$copied_value.find("span").remove();
				var $value = $copied_value.html().trim(); 
				console.log($value + " " + $search_field.val());
				if ($value.indexOf($search_field.val().trim()) == 0) {
					var found = $.inArray($value, inList) > -1;
					if (!found) {
						inList.push($value.trim());
						$dropdown = $facet_index.find('span.tt-dropdown-menu');
						// check if you already got one in the list
						$dropdown.append(
							'<div class=\"tt-dataset-' + index + '\"><span class=\"tt-suggestions\" style=\"display: block;\"><div class=\"tt-suggestion\"><p style=\"white-space: normal;\"><a href="#" class=\"suggested-facet-value\">' + $value + '</a></p></div></span></div>'
						);
					}
					$dropdown.show();
				}
			});
		} else {
			if ($element.val() == '') {
				$facet_index.find('span.tt-dropdown-menu').css('display', 'none');
			}
		}
		applyClicks();
	}
	
	function applyClicks() {
		$('.suggested-facet-value').each(function() {
			$(this).click(function(event) {
				event.preventDefault();
				// copy field
				var $face_value = $(this).html();
//				console.log("$face_value: " + $face_value);
				var $add_more_options = $(this).parent().parent().parent().parent().parent().parent();
				var $add_field = $add_more_options.find('input.form-control.add-facet-field');
				$add_field.val($face_value);
				
				// add to facet list
				$('.add-facet-value').each(function() {
					$(this).click(function(event) {
						event.preventDefault();
						var $link = $(this);
						var $hidden_list = $(this).parent().parent().parent().find('ul.list-unstyled li.facet-options.hide');
						$hidden_list.each(function(index) {
							// find the correct one
							var $anchor = $(this).find('a:nth-child(3)');
							var $copied_value = $anchor.clone();
							$copied_value.find("span").remove();
							$value = $copied_value.html().trim(); 
							var $local_field = $link.parent().parent().find('input.form-control.add-facet-field');
//							console.log(index + " " + $value + " " + $local_field.val());
							if ($local_field.val().trim() == $value) {
								$(this).removeClass('hide');
								$(this).addClass('show');
								$(this).attr('data-attr', 'default');
								console.log('just added, need to reapply show-more links');
//								applyShowMoreLinks();
							}
						});
					});
				});
				
				// remove from facet list
				$('.remove-facet-value').each(function() {
					$(this).click(function(event) {
						event.preventDefault();
						var $link = $(this);
						var $show_list = $(this).parent().parent().parent().find('ul.list-unstyled li.facet-options.show');
						$show_list.each(function(index) {
							// find the correct one
							var $anchor = $(this).find('a:nth-child(3)');
							var $copied_value = $anchor.clone();
							$copied_value.find("span").remove();
							$value = $copied_value.html().trim();
							var $local_field = $link.parent().parent().find('input.form-control.add-facet-field');
							console.log(index + " " + $value + " " + $local_field.val());
							if ($local_field.val().trim() == $value) {
								$(this).removeClass('show');
								$(this).addClass('hide');
							}
						});
					});
				});
			});
		});
	}
	
	$('#add-facet').on('click', function(event) {
		if($('#addFacet').val()) {
			if ($('#search-form').valid()) {
			    $('#modalLoader').modal({
			        backdrop: true,
			        keyboard: true
			    });
			    $('#action').val('add-facet');
				$('#search-form').submit();
			}
		}
	});
	
	$('.facet-remove').each(function() {
		$(this).click(function(event) {
			event.preventDefault();
			if ($('#search-form').valid()) {
			    $('#modalLoader').modal({
			        backdrop: true,
			        keyboard: true
			    });
			}
			var value = $(this).parent().parent().parent().parent().parent().find("input").val();
			console.log(value);
			var action = $("<input>").attr("type", "hidden").attr("name", "action").val("remove-facet");
			var input = $("<input>").attr("type", "hidden").attr("name", "removeFacet").val(value);
			$('#search-form').append($(action));
			$('#search-form').append($(input));
			$('#search-form').submit();
		});
	});
	
	$('.facet-invert').each(function() {
		var parent = $(this).parent().parent().parent().parent().parent()
		var value = parent.find("input").val();
		var url = "search" + window.location.search;
		
//		var facet = "facet.out." + value;
//		url = url.replace("&"+facet, '');
//		url = url + "&" + facet
		
		var facets_inc = parent.parent().find('div.panel-body.' + value + ' div.facet-index ul li a.facet.include span');
		var facets_exc = parent.parent().find('div.panel-body.' + value + ' div.facet-index ul li a.facet.exclude span');

		var facet_value = parent.parent().find('div.panel-body.' + value + ' div.facet-index ul li a.facet-name');

		var list = parent.parent().find('div.panel-body.' + value + ' div.facet-index ul li.facet-options');

		//		div.panel-body.crawl_year div.facet-index ul li.facet-options
		
		$(this).click(function(event) {
//			event.preventDefault();

//			var innerHtml = "(uv) invert this selection";
			var innerHtml = "(+-) Exclude selected values";
			
		    if ($(this).html() === innerHtml ) {
		    	console.log('inverting');

		    	list.each(function() {
					var include = $(this).find('input.include');
					var exclude = $(this).find('input.exclude');
					var facetName = $(this).find('a:nth-child(3)').html();
					if (include.attr('checked')) {
						console.log('Include ' + value + " " + include.attr('checked') + " - " + facetName);
				    	// switch positive to negative
				    	// get include checked
				    	// then switch included to unchecked
				    	// get exclude and switch to checked
						include.removeAttr('checked');
						exclude.attr('checked', 'checked');
					} else {
						exclude.removeAttr('checked');
//						include.attr('checked', 'checked');
					}
				});
		    	
				$('#invert_' + value).val(value);

		    } else {
		    	console.log('back to includes');

		    	list.each(function() {
					var include = $(this).find('input.include');
					var exclude = $(this).find('input.exclude');
					var facetName = $(this).find('a:nth-child(3)').html();
					if (exclude.attr('checked')) {
				    	// switch positive to negative
				    	// get include checked
				    	// then switch included to unchecked
				    	// get exclude and switch to checked
						exclude.removeAttr('checked');
						include.attr('checked', 'checked');
					} else {
						include.removeAttr('checked');
//						exclude.attr('checked', 'checked');
					}
				});

				$('#invert_' + value).val('');
//				url = url.replace(invert, '');
		    }
//		    console.log(url);
//			$(this).attr('href', url);
		    
//		    facetOptions();
		    
		    disableInvertInputs();
			if ($('#search-form').valid()) {
			    $('#modalLoader').modal({
			        backdrop: true,
			        keyboard: true
			    });
			}
			
			$('#search-form').submit();
		});
	});
	
	$('.facet-sort-alpha').each(function() {
		var input = $(this).parent().parent().parent().parent().parent().find("input")
		var value = input.val();
		var url = "search" + window.location.search;

//		?page=1
//		&facet.fields=crawl_year
//		&facet.fields=public_suffix
//		&addFacet=
//		&action=search
//		&query=wikipedia
//		&sort=content_type_norm
//		&order=asc
//		&f.crawl_year.facet.sort=index
		
//		?page=1
//		&facet.fields=crawl_year
//		&facet.fields=public_suffix
//		&addFacet=
//		&action=search
//		&query=wikipedia
//		&sort=content_type_norm
//		&order=asc
//		&action=search
//		&f.crawl_year.facet.sort=count
		
		var facetName = "f." + value + ".facet.sort";
		url = url.replace("&"+facetName+'=count', '').replace("&"+facetName+'=index', '');
		url = url + "&" + facetName + "=index";
		url = url.replace(/page=[0-9]+/, 'page=1');
		$(this).attr('href', url);
		
		$(this).click(function(event) {
			// check if facetName is already in url
			// remove if it is
//			var selectedMenu = $(this).parent().parent().parent().parent().parent().find("input.facet-sort");
//			selectedMenu.val("index");
//			console.log(value + " - " + selectedMenu.val());
//			console.log(url);
			if ($('#search-form').valid()) {
			    $('#modalLoader').modal({
			        backdrop: true,
			        keyboard: true
			    });
			}
			
//			&f.public_suffix.facet.sort=index
//			console.log("f." + value + ".facet.sort=index");
//			var action = $("<input>").attr("type", "hidden").attr("name", "action").val("search");
//			var input = $("<input>").attr("type", "hidden").attr("name", "f." + value + ".facet.sort").val("index");
//			$('#search-form').append($(action));
//			$('#search-form').append($(input));
//			$('#search-form').submit();
		});
	});

	$('.facet-sort-freq').each(function() {
		var value = $(this).parent().parent().parent().parent().parent().find("input").val();
		var url = "search" + window.location.search;

		var facetName = "f." + value + ".facet.sort";
		url = url.replace("&"+facetName+'=count', '').replace("&"+facetName+'=index', '');
		url = url + "&" + facetName + "=count";
		url = url.replace(/page=[0-9]+/, 'page=1');
		$(this).attr('href', url);
		
		$(this).click(function(event) {
			if ($('#search-form').valid()) {
			    $('#modalLoader').modal({
			        backdrop: true,
			        keyboard: true
			    });
			}
			
//			console.log(url);
			
//			&f.public_suffix.facet.sort=count
//			var value = $(this).parent().parent().parent().parent().parent().find("input").val();
//			console.log("f." + value + ".facet.sort=count");
//			var action = $("<input>").attr("type", "hidden").attr("name", "action").val("search");
//			var input = $("<input>").attr("type", "hidden").attr("name", "f." + value + ".facet.sort").val("count");
//			$('#search-form').append($(action));
//			$('#search-form').append($(input));
//			$('#search-form').submit();
		});
	});
	
	$('button#reset-facets').click(function(event){
		var url = window.location.href;
		//removing all facet parameters
		url = url.replace(/\&facet\.([^=]+)\=([^&]+)/g, '');
		url = url.replace(/\&invert\=([^&]*)/g, '');
		url = url.replace(/\&addFacet\=([^&]*)/g, '');
		url = url.replace(/\&action\=remove-facet/g, '');
		url = url.replace(/\&removeFacet\=([^&]+)/g, '');
		document.location.href = url;
	});

});

function getURLParameter(param) {
	var pageUrl = window.location.search.substring(1);
	var urlVariables = pageUrl.split('&');
	for (var i=0; i<urlVariables.length; i++) {
		var parameterName = urlVariables[i].split('=');
		if (parameterName[0] == param) {
			return parameterName[1];
		}
	}				
}

function getURLParameters(param) {
	var values = [];
	var pageUrl = window.location.search.substring(1);
	var urlVariables = pageUrl.split('&');
	for (var i=0; i<urlVariables.length; i++) {
		var parameterName = urlVariables[i].split('=');
		if (parameterName[0] == param) {
			values.push(parameterName[1])
			//return parameterName[1];
		}
	}
	return values;
}

function clearFacets() {
	$(".facet-options").each(function(index) {
		// check form fields
		var $facet_option = $(this);
		var $input_include = $facet_option.find('input.include');
		var $input_exclude = $facet_option.find('input.exclude');

		var $link_span_include = $facet_option.find('a.facet.include span');
		var $link_span_exclude = $facet_option.find('a.facet.exclude span');

		$link_span_include.removeClass('facet-selected');
		$link_span_include.addClass('facet-deselected');

		$link_span_exclude.removeClass('facet-selected');
		$link_span_exclude.addClass('facet-deselected');

		$input_include.removeAttr('checked');
		$input_exclude.removeAttr('checked');
	});			
}

function showFacets() {
	$(".facet-options").each(function(index) {
		// check form fields
		var $facet_option = $(this);
		var $input_include = $facet_option.find('input.include');
		var $input_exclude = $facet_option.find('input.exclude');

		var $link_span_include = $facet_option.find('a.facet.include span');
		var $link_span_exclude = $facet_option.find('a.facet.exclude span');
		
		if ($link_span_include.hasClass("facet-selected") || $link_span_exclude.hasClass("facet-selected")) {
			// go to the beginning and show all
			$ul = $facet_option.parent();
			$ul.find('.facet-options').each(function() {
				var $li = $(this);
				$li.removeClass('hide');
				$li.addClass('show');
			});
		}
	});			
}

function initModal() {
    $('#search-modal-form').validate({
        rules: {
          saveName: {
            required: true
          }
        }
	});
	$('#save-search').on('click', function(event) {
		event.preventDefault();
		$("#save-search-save").prop('disabled', true);
		$("#save-search-form").modal('show');
	    //$('form').attr('action', "/search/save").attr('method', 'post').submit();
	});
	$('#dismiss-search-x').click(function() {
		$("#save-search-form").modal('hide');
	});			
    $('#search-modal-form input').on('keyup blur', function () {
        if ($('#search-modal-form').valid()) {
    		$("#save-search-save").prop('disabled', false);
        } else {
    		$("#save-search-save").prop('disabled', 'disabled');
        }
    });
}

function saveSearch() {
	
	initModal();
	
	$('#save-search-save').on('click', function() {
		var pathArray = window.location.pathname.split('/');
		// first element seems to be blank
		var url = pathArray[2] + window.location.search;
		console.log("url: " + url);
		var summary = $.trim($('#search-summary').html());
		jsRoutes.controllers.Account.saveSearch($('#saveName').val(), $('#save-description').val(), summary, url).ajax({success:successFn, error:errorFn});
		$("#save-search-form").modal('hide');
		$('#saveName').val('');
		$('#save-description').val('');
		var successFn = function(data) {
			console.debug("Success of Ajax Call");
			console.debug(data);
		};
		var errorFn = function(err) {
			console.debug("Error of ajax Call");
			console.debug(err);
		}
		// close and reset form
	});
}

function saveAdvancedSearch() {
	
	initModal();
	
	$('#save-search-save').on('click', function() {
		var pathArray = window.location.pathname.split('/');
		// first element seems to be blank
		var url = pathArray[2] + "/" + pathArray[3] + window.location.search;
		console.log("url: " + url);
		var summary = $.trim($('#search-summary').html());
		jsRoutes.controllers.Account.saveSearch($('#saveName').val(), $('#save-description').val(), summary, url).ajax({success:successFn, error:errorFn});
		$("#save-search-form").modal('hide');
		$('#saveName').val('');
		$('#save-description').val('');
		var successFn = function(data) {
			console.debug("Success of Ajax Call");
			console.debug(data);
		};
		var errorFn = function(err) {
			console.debug("Error of ajax Call");
			console.debug(err);
		}
		// close and reset form
	});
}

function saveCorpus() {
    
    // to open the modal and add events
    $('.save-corpus').each(function() {
    	
    	$(this).on('click', function(event) {
    		event.preventDefault();
			$('input.resource').each(function() {
				$(this).attr('name', 'selectedResource');
			});
    		$("#save-corpus-form").modal('show');
    	});
    	
    	$('#dismiss-corpus-x').click(function() {
			$("#save-resource-save").text('Save Resource(s)');
    		$("#save-corpus-form").modal('hide');
    	});
    	
    	$('#save-corpus-close').click(function() {
			$("#save-resource-save").text('Save Resource(s)');
    		$("#save-corpus-form").modal('hide');
    	});
    	
    });
    
    updateCorpusDropdown();
    validateCorpusName();

    $('#saveCorpusName').on('keyup', function(event) {
    	if($('#saveCorpusName').length > 0) {
    		$("#save-corpus-save").show();
    		$("#save-resource-save").hide();
    	}
    });
    
	// no dropdown
	if($('#saveCorpusName').length > 0) {

		$("#save-corpus-save").show();
		$("#save-resource-save").hide();
		
		$("#save-corpus-save").prop('disabled', true);
		$('#corpus-modal-form input').on('keyup blur', function () {
            if ($('#corpus-modal-form').valid()) {
        		$("#save-corpus-save").prop('disabled', false);
            } else {
        		$("#save-corpus-save").prop('disabled', 'disabled');
            }
        });
	    saveCorpusDetails();
	} else {
		$("#save-corpus-save").hide();
		$("#save-resource-save").show();
		// we have a dropdown to choose from
		saveResourceDetails();
	}

}

function validateCorpusName() {
	
	// validate name
    $('#corpus-modal-form').validate({
        rules: {
        	saveCorpusName: {
                required: {
                    depends: function(element) {
                        return ($('#selectedCorpus').val() == undefined) || ($('#selectedCorpus').length > 0 && $('#selectedCorpus').val().length == 0)
                    }
                }
        	}
        }
	});
}

function updateCorpusDropdown() {
	$('#selectedCorpus').on('change', function(event) {
		event.preventDefault();
	    validateCorpusName();
		console.log($('#selectedCorpus').val().length);
		if ($('#selectedCorpus').val().length > 0) {
			// buttons now change save resource
			$("#save-corpus-save").hide();
			$("#save-resource-save").show();
			saveResourceDetails();
		} else {
			$("#save-corpus-save").show();
			$("#save-resource-save").hide();
			saveCorpusDetails();
		}
	});
}

function saveCorpusDetails() {
	
	$('#save-corpus-save').on('click', function(event) {
		event.preventDefault();
		$("#save-resource-save").text('Save Resource(s)');

		var docs = $('input:checkbox[name="selectedResource"]:checked');
		
		var selectedResources = "";
		docs.each(function() {
			console.log("val: " + $(this).val());
			selectedResources += $(this).val() + ";";
		});
		
		jsRoutes.controllers.Account.saveCorpus($('#saveCorpusName').val(), $('#save-corpus-description').val()).ajax({
			success: function(data) {
				// show dropdown list
				$('#currentCorpusDropDown').remove();
				$('#corpusDropDownPanel').empty();
			    $('#corpusDropDownPanel').append("<div class='form-group'><label for='saveCorpusName' class='col-sm-2 control-label'>Corpus</label><div class='col-sm-10'><div class='dropdown'><select class='form-control' name='selectedCorpus' id='selectedCorpus'><option value='' selected='selected'>&#060;Please Select&#062;</option>");
				$.each($(data), function(key, value) {
					$('#selectedCorpus').append("<option value='" + value.id + "'>" + value.name + "</option>");
				});
				$('#corpusDropDownPanel').append("</select></div></div></div>");
			    updateCorpusDropdown();
			},
		    error: function() {
		    	console.debug("Error of ajax Call");
				console.debug(err);
		    }
		});
		
//		$("#save-corpus-form").modal('hide');
		$('#saveCorpusName').val('');
		$('#save-corpus-description').val('');
		
		// close and reset form
	});
}

function saveResourceDetails() {
	$('#save-resource-save').on('click', function() {
		var docs = $('input:checkbox[name="selectedResource"]:checked');
		var selectedResources = "";
		docs.each(function() {
			console.log("val: " + $(this).val());
			selectedResources += $(this).val().trim() + ",,,,,";
		});
		if (selectedResources.length > 0) {
			jsRoutes.controllers.Account.saveResources($("select#selectedCorpus").val(), selectedResources).ajax({
		          success: function(data) {
		  			console.debug("Success of Ajax Call");
					console.debug(data);
		          },
			      error: function() {
					console.debug("Error of ajax Call");
					console.debug(err);
			      }			
			});
			$("#save-corpus-form").modal('hide');
		} else {
			$("#save-resource-save").text('No resources selected');
			
		}
		// close and reset form
	});
}

function validateSearchForm() {
	
    $('#search-form').validate({
    	errorLabelContainer: "#errorBox",
        rules: {
        	query: "required"
        },
        messages: {
            query: "Please enter search term"
        }
	});
    
}

function validateFacetForm() {
	
    $('#search-form').validate({
        rules: {
        	selectedFacet: {
                required: {
                    depends: function(element) {
                        return $('#action').val() == 'add-facet'
                    }
                }
            }
        },
        messages: {
        	selectedFacet: "Please select a facet"
        }
	});
    
}

function validateAdvancedSearchForm() {
	
    $('#search-form').validate({
        rules: {
        	query: {
        		required: {
                    depends: function(element) {
                        return $('#proximityPhrase1').val().length == 0 && $('#proximityPhrase2').val().length == 0 || $('#proximity').val().length == 0
                    }
        		}
        	},
        	proximityPhrase1: {
                required: {
                    depends: function(element) {
                        return $('#proximityPhrase2').val().length > 0 || $('#proximity').val().length > 0
                    }
                }
            },
            proximityPhrase2: {
                required: {
                    depends: function(element) {
                        return $('#proximityPhrase1').val().length > 0 || $('#proximity').val().length > 0
                    }
                }
            },
        	proximity: {
                required: {
                    depends: function(element) {
                        return $('#proximityPhrase1').val().length > 0 || $('#proximityPhrase2').val().length > 0
                    }
                }
            },
        },
        messages: {
            query: "Please enter search term"
        }
	});
}

//function getMenuChoice(url) {
////	f.crawl_year.facet.sort=index
////	f.public_suffix.facet.sort=index
//    if (url.indexOf('.facet.sort') !== -1 ){
//    	getURLParameter
//}

function csvLink() {
	
	$('#briefCSV').on('click', function(event) {
		event.preventDefault();
		var summary = "";
		$('.summary-li').each(function() {
			summary += $(this).html().trim().replace('<li>','').replace('</li>','') + ", ";
		});
		summary = summary.substring(0, summary.length-2);
		console.log(summary);
		exportMessage();
		var url = $('#export_url').val() + $('#current-url').text() + "&exportType=csv&version=brief&summary="+summary;
		console.log(url);
		window.location.href=url;
	});

	$('#fullCSV').on('click', function(event) {
		event.preventDefault();
		var summary = "";
		$('.summary-li').each(function() {
			summary += $(this).html().trim().replace('<li>','').replace('</li>','') + ", ";
		});
		summary = summary.substring(0, summary.length-2);
		console.log(summary);
		exportMessage();
		var url = $('#export_url').val() + $('#current-url').text() + "&exportType=csv&version=full&summary="+summary;
		console.log(url);
		window.location.href=url;
	});
	
}

// facet (+/-)
function facetOptions() {
	$(".facet-options").each(function(index) {
		// check form fields
		var $facet_option = $(this);
		var $input_include = $facet_option.find('input.include');
		var $input_exclude = $facet_option.find('input.exclude');

		var $link_span_include = $facet_option.find('a.facet.include span');
		var $link_span_exclude = $facet_option.find('a.facet.exclude span');

		var url = decodeURIComponent(window.location.search);
		var url2 = url;

		// rework this bit
		// if invert is selected
		var facetClass = $input_include.attr('name').replace('facet.in.', '');
//		console.log("facetClass: " + facetClass);

		if (!url.indexOf("facet.in") >= 0 || !url.indexOf("facet.out")) {
			$link_span_include.removeClass('hide');
			$link_span_exclude.addClass('hide');
		}

		var inverts = getURLParameters('invert');
		
		for (var i=0; i < inverts.length; i++) {
			var facet = inverts[i];
			var idfacet = '#invert_' + facet;
			var input = $(idfacet);
//			console.log(idfacet + " - " + input.val() + " - " + facetClass);
			if (facet == facetClass) {
				$link_span_include.addClass('hide');
				$link_span_exclude.removeClass('hide');
			}
		}
		
		// to read back facet options after submit
		facetToggle($input_include, $link_span_include);
		facetToggle($input_exclude, $link_span_exclude);

		var facet_name_inc = $input_include.attr('name');
		var facet_value_inc = $input_include.val();

		var facet_name_exc = $input_exclude.attr('name');
		var facet_value_exc = $input_exclude.val();

		// for clicking on facet options (includes)
		$(this).find('a.facet.include').each(function(index) {
			//console.log(facet_name + " " + facet_value);

			var span = $(this).find('span.glyphicon');
			
			var facet = facet_name_inc + "=" + facet_value_inc;
			if (!span.hasClass('facet-selected')) {
				// SELECTED
				url = url + "&" + facet;
			} else {
				// facet selected
				// i.e remove facet.in.crawl_year=%222007%22
				var regexInc = new RegExp("&"+ facet);
				url = url.replace(regexInc,'');
//				console.log(url);
//				console.log("remove facet: " + facet);
			}
			url = url.replace('&invert=&', '&');
			url = url.replace(/page=[0-9]+/, 'page=1');
			$(this).attr('href', url);
			
			$(this).click(function(event) {
//						event.preventDefault();
				// change +/-
				facetClickToggle($link_span_include, $input_include);
				//console.log($('#search-form').serialize());
				
				if ($('#search-form').valid()) {
				    $('#modalLoader').modal({
				        backdrop: true,
				        keyboard: true
				    });
//							$('#search-form').submit();
					// switch to -
					
					
				}
			});

		});
		
		//reset url before handling exclude
		url = url2;
		
		// for clicking on facet options (excludes)
		$(this).find('a.facet.exclude').each(function(index) {
			//console.log(facet_name + " " + facet_value);
			var span = $(this).find('span.glyphicon');
			//console.log(url);
			var facet = facet_name_exc + "=" + facet_value_exc;
			if (!span.hasClass('facet-selected')) {
				// SELECTED
				url = url + "&" + facet;
			} else {
				// i.e remove facet.in.crawl_year=%222007%22
				var regexExc = new RegExp("&"+ facet);
				url = url.replace(regexExc,'');
//						console.log("1) " + url);
//						console.log("remove facet: " + facet);
			}
			
			url = url.replace('&invert=&', '&');
//			console.log("2) " + url);
			url = url.replace(/page=[0-9]+/, 'page=1');
			$(this).attr('href', url);
			
			$(this).click(function(event) {
//						event.preventDefault();
				// change +/-
				facetClickToggle($link_span_exclude, $input_exclude);
				
				if ($('#search-form').valid()) {
				    $('#modalLoader').modal({
				        backdrop: true,
				        keyboard: true
				    });
//							$('#search-form').submit();
					// switch to +
					
					
				}
			});

		});
	});
}

function applyInverts() {
	var inverts = getURLParameters('invert');
	for (i=0; i < inverts.length; i++) {
		var facet = inverts[i];
		var innerHtml = "(+-) Include selected values";
		var input = $('#invert_' + facet);
		var menu = $('#invertmenu_' + facet);
		input.val(facet);
		input.addClass('inverted');
		menu.addClass(facet);
		innerHtml += " <span class='glyphicon glyphicon-ok'></span>";
		menu.html(innerHtml);
	}
}

function facetToggle($element, $button) {
	if ($element.attr('checked') !== undefined) {
		$button.removeClass('facet-deselected');
		$button.addClass('facet-selected');
	} else {
		$button.removeClass('facet-selected');
		$button.addClass('facet-deselected');
	}
}

function facetClickToggle($button, $element) {
	//console.log($button.attr('class') + " " + $element.attr('class'));
	if ($button.hasClass('facet-deselected')) {
		// SELECTED
		$button.removeClass('facet-deselected');
		$button.addClass('facet-selected');
		$element.attr('checked', '');
	} else {
		// DESELECTED
		$button.removeClass('facet-selected');
		$button.addClass('facet-deselected');
		$element.removeAttr('checked');
	}
}

function disableInvertInputs() {
	$("input[name='invert']").each(function() {
	    if ($(this).val() == '') {
			$(this).prop('disabled', true);
	    }
	});
}

function searchTabs() {
	var tab = getURLParameter('tab');
	$('#tab').val(tab);
	
	if (tab == 'concordance') {
		$('#concordanceTab').addClass('active');
		$('#resultsTab').removeClass('active');
		$('#results').hide();
		$('#concordance').show();
	} else {
		$('#resultsTab').addClass('active');
		$('#concordanceTab').removeClass('active');
		$('#concordance').hide();
		$('#results').show();
	}
	
	$('#resultsTab a').click(function (e) {
		e.preventDefault()
		$(this).tab('show');
		$('#concordance').hide();
		$('#results').show();
		$('#tab').val('results');
	});
	
	$('#concordanceTab a').click(function (e) {
		e.preventDefault()
		$(this).tab('show');
		$('#results').hide();
		$('#concordance').show();
		$('#tab').val('concordance');
	});
}

function modalLoader() {
	$('#modalLoader').modal({
	    backdrop: true,
	    keyboard: true
	});
}

function getMonthName(monthNumber) {
	var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
	              'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
	return months[monthNumber];
}

function createSummaryExclusions() {
	var excludes = getURLParameters('exclude');
//	console.log("excludes: " + excludes);
	// with these resources create some hidden inputs
	excludes.forEach(function(value) {
		value = value.trim();
		var values = decodeURIComponent(value).split(';;;');
//		console.log(id);
		var dateString = values[1].trim().replace(/\+/g, " ").replace("BST", "");
		var title = values[2].trim().replace(/\+/g, " ");
		var domain = values[3].trim().replace(/\+/g, " ");
		var span = " <span class='glyphicon glyphicon-remove-sign removeExcluded' removeID='" + value + "'></span>";

//		var testDate = "Sat May 06 23:35:01 BST 2006";
//		var d = new Date(testDate);
//		console.log("d: " + d);
		
		var date = new Date(dateString);
		console.log(dateString + " " + date + " " + title + " " + domain);
		
		// format Thu Nov 14 03:28:36 GMT 1999
		// now May 14 2008
		var dateFormatted = getMonthName(date.getMonth()) + " " + date.getDate() + " " + date.getFullYear();

		var text = $('<li>').html("Exclude: " + domain + " - " + title + " - " + dateFormatted + span);
		//Exclude: telegraph.co.uk - Wikipedia ends unrestricted editing of articles -Telegraph - May 14 2008
		$('#search-summary').append(text);
	});
	
	var excludeHosts = getURLParameters('excludeHost');
	excludeHosts.forEach(function(value) {
		var host = decodeURIComponent(value)
//		console.log(id);
		var span = " <span class='glyphicon glyphicon-remove-sign removeExcluded' removeID='" + host + "'></span>";
		var text = $('<li>').html("Exclude Host: " + host + span);
		$('#search-summary').append(text);
	});
}

function processSelectedResources(selected) {
	// selected can be id or host name
	
	var resources = getURLParameters('selectedResource');
	var currentExcludes = getURLParameters('exclude');
	var currentExcludeHosts = getURLParameters('excludeHost');
	var facetFields = getURLParameters('facet.fields');
	var invert = getURLParameters('invert');
	var order = getURLParameters('order');
	
	console.log(resources);
	console.log(currentExcludes);
	console.log(currentExcludeHosts);
	console.log(facetFields);
	console.log(invert);
	console.log(order);
	
	// with these resources create some hidden inputs
	
	if (selected !== undefined) {
		for (var i=currentExcludes.length-1; i>=0; i--) {
		    if (decodeURIComponent(currentExcludes[i]) === selected) {
				console.log("removing exclude " + selected);
				currentExcludes.splice(i, 1);
		        break;
		    }
		}
		
		for (var i=currentExcludeHosts.length-1; i>=0; i--) {
		    if (decodeURIComponent(currentExcludeHosts[i]) === selected) {
				console.log("removing exclude hosts " + selected);
				currentExcludeHosts.splice(i, 1);
		        break;
		    }
		}
		
	}
	console.log("updated excludes: " + currentExcludes);
	console.log("updated exclude hosts: " + currentExcludeHosts);
	
	resources.forEach(function(value) {
		var id = decodeURIComponent(value)
		var selectedResource = $("<input>")
   			.attr("type", "hidden")
   			.attr("name", "selectedResource").val(id);
    
    	$('#search-form').append($(selectedResource));
	});
	
	currentExcludes.forEach(function(value) {
		var id = decodeURIComponent(value)
		var selectedResource = $("<input>")
   			.attr("type", "hidden")
   			.attr("name", "exclude").val(id);
    
    	$('#search-form').append($(selectedResource));
	});
	
	currentExcludeHosts.forEach(function(value) {
		var id = decodeURIComponent(value)
		var selectedResource = $("<input>")
   			.attr("type", "hidden")
   			.attr("name", "excludeHost").val(id);
    
    	$('#search-form').append($(selectedResource));
	});	

}

function resetExcluded() {
	$('span.glyphicon.glyphicon-remove-sign.removeExcluded').on('click', function(event) {
		// remove it from parameters
		// submit
		var removeID = $(this).attr('removeID');
		processSelectedResources(decodeURIComponent(removeID));
		modalLoader();
	    $('#action').val('search');
	    // add to parameters then submit
	    // &facet.fields=postcode_district
		$('#search-form').submit();
	});
}

function exportMessage() {
	$('#export-message').removeClass('hide');
	$('#export-message').append('Exporting to CSV - This may take a while. Please wait...');
}

function applyShowMoreLinks() {
	$('.show-more').each(function(index) {
		var $show_more = $(this);
		var parent = $(this).parent().parent().parent();
		
		$(this).click(function(event) {
			event.preventDefault();
			// click on link and do something....
			parent.parent().find('li.facet-options').each(function(index) {
				$li = $(this);
				var $show_more_icon = $show_more.find('span:nth-child(1)');
				var $show_more_span = $show_more.find('span:nth-child(2)');
				var $link_text = $show_more.find('span:nth-child(2)').html();
				var $default_show = $li.attr('data-attr');
				var $name = $li.find('a:nth-child(3)').html();
				
				console.log($name + " - " + $li.attr('class') + " - " + $default_show);
				
				if ($li.hasClass('hide')) {
					$li.addClass('show');
					$li.removeClass('hide');
					$show_more_icon.removeClass('glyphicon-plus-sign');
					$show_more_icon.addClass('glyphicon-minus-sign');
					$show_more_span.html("Hide");
				} else if ($li.hasClass('show') && $default_show !== 'default') {
					$li.addClass('hide');
					$li.removeClass('show');
					$show_more_icon.removeClass('glyphicon-minus-sign');
					$show_more_icon.addClass('glyphicon-plus-sign');
					$show_more_span.html("Show more...");
				} else if ($li.hasClass('show') && $default_show == 'user-add') {
					console.log('here');
					$li.addClass('show');
					$li.removeClass('hide');
					$show_more_icon.removeClass('glyphicon-minus-sign');
					$show_more_icon.addClass('glyphicon-plus-sign');
				} 
			});
		});
	});
}

function resetFacets() {
	$("input[name='facet.fields']").each(function() {
		var facet = $(this);
		if (facet.val() == "crawl_year" || facet.val() == "public_suffix") {
//			console.log("not removing: " + facet.val());
		} else {
//			console.log("removing: " + facet.val());
//			facet.remove();
		}
	});
}
// Called when user changes sort selector
function setSort(el) {
	var index = el.options.selectedIndex;
	var order = el.options[index].getAttribute('data-order');
	$("#order").val(order);
}

//Set sort selctor to current order value after page reload
function setSortOption(currentSort, currentOrder) {
	$("#sort").find("option[data-order='" + currentOrder + "'][value='" + currentSort + "']").attr("selected", true);
}

