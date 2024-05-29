package carefree.CarefreeOCR.controller.form.construct;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/molit")
public class MolitFormController {

    @GetMapping()
    public String getMolitIndexPage() {
        return "construct/molit-form";
    }
}
