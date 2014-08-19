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
	
	$('#sort').change(function(event) {
		console.log('sort');
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

	$(".add-more-button").each(function(index) {
		$(this).click(function(event) {
			event.preventDefault();
			var addMoreSelector = "#add-more-option-" + index;
			var buttonText = "#add-more-button-text-" + index;
			if ($(addMoreSelector).hasClass('hide')) {
				$(addMoreSelector).removeClass('hide');
				$(buttonText).html("Close")
			} else {
				$(addMoreSelector).addClass('hide');
				$(buttonText).html("Add")
			}
		});
	});
	
	$('.show-more').each(function(index) {
		var $show_more = $(this);
		$(this).click(function(event) {
			event.preventDefault();
			// click on link and do something....
			$(this).parent().parent().find('li.facet-options').each(function(index) {
				$li = $(this);
				var $show_more_icon = $show_more.find('span:nth-child(1)');
				var $show_more_span = $show_more.find('span:nth-child(2)');
				var $link_text = $show_more.find('span:nth-child(2)').html();
				var $default_show = $li.attr('data-attr');
				if ($li.hasClass('hide')) {
					$li.addClass('show');
					$li.removeClass('hide');
					$show_more_icon.removeClass('glyphicon-plus-sign');
					$show_more_icon.addClass('glyphicon-minus-sign');
					$show_more_span.html("Hide");
				} 
				else if ($li.hasClass('show') && $default_show !== 'default') {
					$li.addClass('hide');
					$li.removeClass('show');
					$show_more_icon.removeClass('glyphicon-minus-sign');
					$show_more_icon.addClass('glyphicon-plus-sign');
					$show_more_span.html("Show more...");
				}
			});
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
				console.log("$face_value: " + $face_value);
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
							console.log(index + " " + $value + " " + $local_field.val());
							if ($local_field.val().trim() == $value) {
								$(this).removeClass('hide');
								$(this).addClass('show');
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
		var value = $(this).parent().parent().parent().parent().parent().find("input").val();
		var url = "search" + window.location.search;
		
//		var facet = "facet.out." + value;
//		url = url.replace("&"+facet, '');
//		url = url + "&" + facet
//		$(this).attr('href', url);

		$(this).click(function(event) {
			var facets_inc = $(this).parent().parent().parent().parent().parent().parent().find('div.panel-body.' + value + ' div.facet-index ul li a.facet.include span');
			var facets_exc = $(this).parent().parent().parent().parent().parent().parent().find('div.panel-body.' + value + ' div.facet-index ul li a.facet.exclude span');

			var invert = $("<input>").attr("type", "hidden").attr("name", "invert").attr('id','invert-'+value);

			var innerHtml = "(uv) invert this selection";
		    if ($(this).html() === innerHtml ) {
				$(this).addClass(value);
		    	innerHtml += " <span class='glyphicon glyphicon-ok'></span>";
				// add selected
			    facets_inc.each(function() {
					$(this).addClass('hide');
				});
				facets_exc.each(function() {
					$(this).removeClass('hide');
				});
				$(this).html(innerHtml);
				invert.val(value)
				$('#search-form').append($(invert));

		    } else {
				$(this).removeClass(value);
		    	innerHtml = innerHtml.replace(" <span class='glyphicon glyphicon-ok'></span>", '');
			    facets_inc.each(function() {
					$(this).removeClass('hide');
				});
				facets_exc.each(function() {
					$(this).addClass('hide');
				});
				$(this).html(innerHtml);
				$('#invert-'+value).remove();
		    }
		    
		    facetOptions();
//			if ($('#search-form').valid()) {
//			    $('#modalLoader').modal({
//			        backdrop: true,
//			        keyboard: true
//			    });
//			}
			
//			var action = $("<input>").attr("type", "hidden").attr("name", "action").val("invert-facet");
//			var input = $("<input>").attr("type", "hidden").attr("name", "removeFacet").val(value);
//			$('#search-form').append($(action));
//			$('#search-form').append($(input));
//			$('#search-form').submit();
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
    $('#modal-form').validate({
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
	$('#dismiss-x').click(function() {
		$("#save-search-form").modal('hide');
	});			
    $('#modal-form input').on('keyup blur', function () {
        if ($('#modal-form').valid()) {
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
        	query: "required",
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
	$('#csv-export').on('click', function(event) {
		event.preventDefault();
		window.location.href=$('#export_url').val() + $('#current-url').text();
	});
}

function facetOptions() {
	$(".facet-options").each(function(index) {
		// check form fields
		var $facet_option = $(this);
		var $input_include = $facet_option.find('input.include');
		var $input_exclude = $facet_option.find('input.exclude');

		var $link_span_include = $facet_option.find('a.facet.include span');
		var $link_span_exclude = $facet_option.find('a.facet.exclude span');

		var url = window.location.search;

		// rework this bit
		// if invert is selected
		var facetClass = $input_include.attr('name').replace('facet.in.', '');
		
		if ($('#invert-'+facetClass).val() == facetClass) {
			console.log("invert: " + $('#invert-'+facetClass).val() + " " + facetClass);
			$link_span_include.addClass('hide');
			$link_span_exclude.removeClass('hide');
		}
		else if (!url.indexOf("facet.in") >= 0 || !url.indexOf("facet.out")) {
			$link_span_include.removeClass('hide');
			$link_span_exclude.addClass('hide');
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
			var url = "search?" + $('#search-form').serialize();
			// if not activated then remove
/* 					facet_value_exc = facet_value_exc.replace('"', '%22').replace('"', '%22');
			var regexExc = new RegExp("&"+ facet_name_exc + "=" + facet_value_exc);
*/					
//					console.log("remove: " + "&"+ facet_name_exc + "=" + facet_value_exc);
//					url = url.replace(regexExc,'');
//					console.log("new url: " + url);

			var span = $(this).find('span.glyphicon');
			
			var facet = facet_name_inc + "=" + facet_value_inc;
			if (!span.hasClass('facet-selected')) {
				// SELECTED
				url = url + "&" + facet;
			} else {
				// i.e remove facet.in.crawl_year=%222007%22
				facet = facet.replace('"', '%22').replace('"', '%22');
//						console.log("facet_value: " + facet_value);
				var regexInc = new RegExp("&"+ facet);
				url = url.replace(regexInc,'');
//						console.log(url);
//						console.log("remove facet: " + facet);
			}
			
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
		
		// for clicking on facet options (excludes)
		$(this).find('a.facet.exclude').each(function(index) {
			//console.log(facet_name + " " + facet_value);
			var url = "search?" + $('#search-form').serialize();
			// if not activated then remove
/* 					facet_value_inc = facet_value_inc.replace('"', '%22').replace('"', '%22');
			var regexInc = new RegExp("&"+ facet_name_inc + "=" + facet_value_inc); */
			
//					console.log("remove: " + "&"+ facet_name_inc + "=" + facet_value_inc);
//					url = url.replace(regexInc,'');
//					console.log("new url: " + url);


			var span = $(this).find('span.glyphicon');
			
			var facet = facet_name_exc + "=" + facet_value_exc;
			if (!span.hasClass('facet-selected')) {
				// SELECTED
				url = url + "&" + facet;
			} else {
				// i.e remove facet.in.crawl_year=%222007%22
				facet = facet.replace('"', '%22').replace('"', '%22');
				var reg = "^\".*\"$";
//						console.log("facet_value: " + facet_value);
				var regexExc = new RegExp("&"+ facet);
				url = url.replace(regexExc,'');
//						console.log(url);
//						console.log("remove facet: " + facet);
			}
			
			$(this).attr('href', url);
			
			$(this).click(function(event) {
//						event.preventDefault();
				// change +/-
				facetClickToggle($link_span_exclude, $input_exclude);
//						console.log($('#search-form').serialize());
				
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