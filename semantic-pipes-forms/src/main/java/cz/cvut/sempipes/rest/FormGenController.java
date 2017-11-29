package cz.cvut.sempipes.rest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/formGen")
public class FormGenController { //TODO extends BaseController {

//    @Autowired
//    private FormGenService formGenService;
//
//    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public RawJson generateForm(@RequestBody PatientRecord data) {
//        return formGenService.generateForm(data);
//    }
//
//    @RequestMapping("/possibleValues")
//    public RawJson getPossibleValues(@RequestParam("query") String query) {
//        return formGenService.getPossibleValues(query);
//    }
}
