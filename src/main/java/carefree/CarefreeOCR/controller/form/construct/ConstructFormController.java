package carefree.CarefreeOCR.controller.form.construct;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/construct")
public class ConstructFormController {

    @GetMapping
    public String getConstructForm() {
        return "construct/construct-home";
    }

    @GetMapping(value = "/molit")
    public String getMolitIndexPage() {
        return "construct/molit-form";
    }

    @GetMapping(value = "/kica")
    public String getKicaIndexPage() {
        return "construct/kica-form";
    }

    @GetMapping(value = "/ecic")
    public String getEcicIndexPage() {
        return "construct/ecic-form";
    }

    @GetMapping(value = "/ekffa")
    public String getEkffaIndexPage() {
        return "construct/ekffa-form";
    }
}
