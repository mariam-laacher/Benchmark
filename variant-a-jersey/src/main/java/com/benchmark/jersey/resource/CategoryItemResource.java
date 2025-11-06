package com.benchmark.jersey.resource;

import com.benchmark.common.entity.Item;
import com.benchmark.common.dto.PageResult;
import com.benchmark.jersey.dao.ItemDAO;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/categories/{categoryId}/items")
@Produces(MediaType.APPLICATION_JSON)
public class CategoryItemResource {
    @Inject
    private ItemDAO itemDAO;

    @GET
    public Response getItemsByCategory(
            @PathParam("categoryId") Long categoryId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        PageResult<Item> result = itemDAO.findByCategoryId(categoryId, page, size);
        return Response.ok(result).build();
    }
}
