/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
$(function () {

    $(document).ready(function () {
        $("#add").click(function () {
            var lastField = $("#buildyourform div:last");
            var intId = (lastField && lastField.length && lastField.data("idx") + 1) || 1;
            var fieldWrapper = $("<div class=\"fieldwrapper\" id=\"field" + intId + "\"/>");
            fieldWrapper.data("idx", intId);
            var fName = $("<input type='file' name='file' class=\"fieldname\">");
            var removeButton = $("<input type=\"button\" class=\"remove\" value=\"X\" />");
            removeButton.click(function () {
                $(this).parent().remove();
            });
            fieldWrapper.append(fName);
            fieldWrapper.append(removeButton);
            $("#buildyourform").append(fieldWrapper);
        });

    });

});



