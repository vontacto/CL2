package org.cibertec.edu.pe.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.cibertec.edu.pe.model.Detalle;
import org.cibertec.edu.pe.model.Producto;
import org.cibertec.edu.pe.model.Compra; 
import org.cibertec.edu.pe.model.DetCompra; 
import org.cibertec.edu.pe.repository.IProductoRepository;
import org.cibertec.edu.pe.repository.ICompraRepository; 
import org.cibertec.edu.pe.repository.IDetCompraRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes({"carrito", "total"})
public class ProductoController {

    @Autowired
    private IProductoRepository productoRepository;
    
    @Autowired
    private ICompraRepository compraRepository; 
    
    @Autowired
    private IDetCompraRepository detCompraRepository;

    @GetMapping("/index")
    public String listado(Model model) {
        List<Producto> lista = productoRepository.findAll();
        model.addAttribute("productos", lista);
        return "index";
    }

    @GetMapping("/agregar/{idProducto}")
    public String agregar(Model model, HttpSession session, @PathVariable(name = "idProducto") int idProducto) {
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        if (producto != null) {
            List<Detalle> carrito = (List<Detalle>) session.getAttribute("carrito");
            if (carrito == null) {
                carrito = new ArrayList<>();
            }
            Detalle detalle = new Detalle();
            detalle.setProducto(producto);
            detalle.setCantidad(1);
            detalle.setSubtotal(producto.getPrecio());
            carrito.add(detalle);
            session.setAttribute("carrito", carrito);
        }
        return "redirect:/index";
    }

    @GetMapping("/carrito")
    public String verCarrito(Model model, HttpSession session) {
        List<Detalle> carrito = (List<Detalle>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
        }
        model.addAttribute("carrito", carrito);
        return "carrito";
    }

    @GetMapping("/pagar")
    public String pagar(Model model, HttpSession session) {
        List<Detalle> carrito = (List<Detalle>) session.getAttribute("carrito");
        
        double total = 0.0;
        if (carrito != null) {
            for (Detalle detalle : carrito) {
                total += detalle.getSubtotal();
            }
        }

        if (carrito != null) {
            Compra compra = new Compra();
            compra.setFechaCompra(new Date()); 
            compra = compraRepository.save(compra); 
            for (Detalle detalle : carrito) {
                DetCompra detCompra = new DetCompra();
                detCompra.setCompra(compra);
                detCompra.setProducto(detalle.getProducto());
                detCompra.setCantidad(detalle.getCantidad());
                detCompra.setSubtotal(detalle.getSubtotal());
                detCompra = detCompraRepository.save(detCompra); 
            }
        }
        
        session.removeAttribute("carrito");
        session.removeAttribute("total");
        
        model.addAttribute("total", total);
        
        return "pagar";
    }

    @PostMapping("/actualizarCarrito")
    public String actualizarCarrito(Model model, HttpSession session,
                                     @RequestParam(name = "idProducto") int idProducto,
                                     @RequestParam(name = "cantidad") int cantidad) {
        List<Detalle> carrito = (List<Detalle>) session.getAttribute("carrito");
        if (carrito != null) {
            for (Detalle detalle : carrito) {
                if (detalle.getProducto().getIdProducto() == idProducto) {
                    detalle.setCantidad(cantidad);
                    detalle.setSubtotal(detalle.getProducto().getPrecio() * cantidad);
                    break;
                }
            }
            session.setAttribute("carrito", carrito);
        }
        return "redirect:/carrito";
    }

    @ModelAttribute("carrito")
    public List<Detalle> getCarrito() {
        return new ArrayList<Detalle>();
    }
    
    @ModelAttribute("total")
    public double getTotal() {
        return 0.0;
    }
}